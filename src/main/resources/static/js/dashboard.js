let currentDashboardIssue = null;
// 全局模态框实例缓存（避免重复创建）
let modalInstances = {
    viewDashTransactionModal: null,
    addDashUpdateModal: null
};

/**
 * 初始化Dashboard：加载统计数据、渲染图表、加载近期事务
 */
function initDashboard() {
    // 1. 加载用户相关事务统计数据
    loadIssueStatistics();
    // // 2. 加载并渲染项目分布柱状图（ECharts版）
    // loadProjectDistributionChart();
    // // 3. 加载并渲染状态占比饼图（ECharts版）
    // loadStatusRatioChart();
    // 4. 加载近期事务表格
    loadRecentIssues();
    // 5. 加载甘特图
    loadGanttChart();

    loadDashExecutors();
    initDashUpdateEvents();
}

function loadDashExecutors() {
    const updateExecutor = document.getElementById('updateExecutor_dash');
    const updateLeader = document.getElementById('updateLeader_dash');

    if (!updateExecutor || !updateLeader) return;

    fetch('/api/user/userlist')
        .then(response => response.json())
        .then(users => {
            // 按用户姓名排序
            const sortedUsers = [...users].sort((a, b) => {
                return a.fullName.localeCompare(b.fullName, 'zh-CN');
            });

            // 用于存储原始选项，供搜索功能使用
            const userOptionsData = [];

            sortedUsers.forEach(user => {
                const optionText = user.fullName;
                const optionValue = user.userIndex;

                // 存储选项数据
                userOptionsData.push({ text: optionText, value: optionValue });

                const selectOption1 = document.createElement('option');
                selectOption1.value = optionValue;
                selectOption1.textContent = optionText;
                updateExecutor.appendChild(selectOption1);

                const selectOption2 = document.createElement('option');
                selectOption2.value = optionValue;
                selectOption2.textContent = optionText;
                updateLeader.appendChild(selectOption2);
            });
        })
        .catch(error => console.error('加载执行人列表失败:', error));
}

/**
 * 加载当前用户事务统计数据（总数量、各状态数量）
 */
function loadIssueStatistics() {
    fetch(`/api/dashboard/statistics?userId=${window.currentUser}`)
        .then(response => {
            if (!response.ok) throw new Error('获取统计数据失败');
            return response.json();
        })
        .then(statistics => {
            // 更新统计卡片数值
            document.getElementById('totalIssues').textContent = statistics.total || 0;
            document.getElementById('completedIssues').textContent = statistics.open || 0;
            document.getElementById('inProgressIssues').textContent = statistics.progress || 0;
            document.getElementById('overdueIssues').textContent = statistics.overdue || 0;
        })
        .catch(error => {
            console.error('加载事务统计失败:', error);
            // 异常处理：显示默认值
            ['totalIssues', 'completedIssues', 'inProgressIssues', 'overdueIssues'].forEach(id => {
                document.getElementById(id).textContent = '0';
            });
        });
}

/**
 * 渲染「事务项目分布」柱状图（ECharts版）
 */
function loadProjectDistributionChart() {
    fetch(`/api/dashboard/project-distribution?userId=${window.currentUser}`)
        .then(response => {
            if (!response.ok) throw new Error('获取项目分布数据失败');
            return response.json();
        })
        .then(data => {
            // 提取项目名称和对应事务数量
            const projectNames = data.map(item => item.projectName);
            const issueCounts = data.map(item => item.count);

            // 初始化ECharts实例
            const chartDom = document.getElementById('issueProjectChart');
            const myChart = echarts.init(chartDom);

            // ECharts配置项
            const option = {
                title: {
                    text: '事务项目分布',
                    left: 'center',
                    textStyle: { fontSize: 14 }
                },
                tooltip: {
                    trigger: 'axis',
                    axisPointer: { type: 'shadow' }
                },
                grid: {
                    left: '3%',
                    right: '4%',
                    bottom: '3%',
                    containLabel: true
                },
                xAxis: {
                    type: 'category',
                    data: projectNames,
                    axisLabel: {
                        rotate: 15, // 标签旋转，避免项目名过长重叠
                        fontSize: 12
                    }
                },
                yAxis: {
                    type: 'value',
                    name: '事务数量',
                    min: 0
                },
                series: [{
                    name: '事务数量',
                    type: 'bar',
                    data: issueCounts,
                    itemStyle: {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                            { offset: 0, color: '#36a2eb' },
                            { offset: 1, color: '#8bc34a' }
                        ])
                    },
                    barWidth: '60%'
                }]
            };

            // 渲染图表
            myChart.setOption(option);
            // 自适应窗口大小变化
            window.addEventListener('resize', () => myChart.resize());
        })
        .catch(error => {
            console.error('渲染项目分布图表失败:', error);
            document.getElementById('issueProjectChart').innerHTML = '<div class="text-center text-danger">图表加载失败</div>';
        });
}

/**
 * 渲染「事务状态占比」饼图（ECharts版）
 */
function loadStatusRatioChart() {
    fetch(`/api/dashboard/status-ratio?userId=${window.currentUser}`)
        .then(response => {
            if (!response.ok) throw new Error('获取状态占比数据失败');
            return response.json();
        })
        .then(data => {
            // 提取状态名称、数量，配置配色
            const statusData = data.map(item => ({
                name: item.statusName,
                value: item.count
            }));
            const colorList = [
                '#198754', // 已关闭
                '#d63384', // 开启
                '#ffc107', // 进行中
                '#20c997',  // 待验证
                '#6c757d'   // 待提交
            ];

            // 初始化ECharts实例
            const chartDom = document.getElementById('issueStatusChart');
            const myChart = echarts.init(chartDom);

            // ECharts配置项
            const option = {
                title: {
                    text: '事务状态占比',
                    left: 'center',
                    textStyle: { fontSize: 14 }
                },
                tooltip: {
                    trigger: 'item',
                    formatter: '{a} <br/>{b}: {c} ({d}%)' // 显示名称、数量、百分比
                },
                legend: {
                    orient: 'vertical',
                    right: 10,
                    top: 'center',
                    textStyle: { fontSize: 12 }
                },
                series: [{
                    name: '事务状态',
                    type: 'pie',
                    radius: ['40%', '70%'], // 环形饼图（如需实心饼图，设为 ['0%', '70%']）
                    avoidLabelOverlap: false,
                    itemStyle: {
                        borderRadius: 8,
                        borderColor: '#fff',
                        borderWidth: 2
                    },
                    label: {
                        show: false,
                        position: 'center'
                    },
                    emphasis: {
                        label: {
                            show: true,
                            fontSize: 14,
                            fontWeight: 'bold'
                        }
                    },
                    labelLine: { show: false },
                    data: statusData,
                    color: colorList.slice(0, data.length)
                }]
            };

            // 渲染图表
            myChart.setOption(option);
            // 自适应窗口大小变化
            window.addEventListener('resize', () => myChart.resize());
        })
        .catch(error => {
            console.error('渲染状态占比图表失败:', error);
            document.getElementById('issueStatusChart').innerHTML = '<div class="text-center text-danger">图表加载失败</div>';
        });
}

/**
 * 加载近期事务表格（按截止日期排序）
 */
function loadRecentIssues() {
    const tableBody = document.getElementById('recentIssuesContainer');
    tableBody.innerHTML = '<tr><td colspan="6" class="text-center">加载中...</td></tr>';

    fetch(`/api/dashboard/recent?userId=${window.currentUser}&size=10`) // 只加载前10条
        .then(response => {
            if (!response.ok) throw new Error('获取近期事务失败');
            return response.json();
        })
        .then(issues => {
            if (issues.length === 0) {
                tableBody.innerHTML = '<tr><td colspan="6" class="text-center">暂无相关事务</td></tr>';
                return;
            }

            // 渲染表格行
            let html = '';
            issues.forEach(issue => {
                // 状态样式映射
                const statusMap = {
                    'OPEN': { class: 'column-open', text: '开启' },
                    'IN PROGRESS': { class: 'column-progress', text: '进行中' },
                    'CLOSED': { class: 'bg-success', text: '已关闭' },
                    'VALIDATION': { class: 'column-validation', text: '待验证' },
                    'PENDING': { class: 'column-pending', text: '待提交' }
                };
                const priorityMap = {
                    'HIGH': { class: 'bg-danger', text: '高' },
                    'MID': { class: 'bg-warning', text: '中' },
                    'LOW': { class: 'bg-success', text: '低' }
                };
                const status = statusMap[issue.issueStatusName] || { class: 'bg-secondary', text: issue.issueStatusName };
                const priority = priorityMap[issue.priorityName] || { class: 'bg-success', text: issue.priorityName };
                const description = currentUserLang.includes('zh') ? issue.description_cn : currentUserLang.includes('en') ? issue.description_en : issue.description;

                // 格式化截止日期
                const deadline = formatDate(issue.deadline);

                html += `
                    <tr>
                        <td>${issue.projectName || '-'}</td>
                        <td>${description || '-'}</td>
                        <td class="text-center"><span class="badge ${priority.class}">${priority.text}</span></td>
                        <td class="text-center"><span class="badge ${status.class}">${status.text}</span></td>
                        <td class="text-center">${formatDate(issue.deadline)}</td>
                        <td class="text-center">
                            <button class="btn btn-sm btn-primary view-issue" data-id="${issue.issueIndex}">查看</button>
                        </td>
                    </tr>
                `;
            });
            tableBody.innerHTML = html;
        })
        .catch(error => {
            console.error('加载近期事务失败:', error);
            tableBody.innerHTML = '<tr><td colspan="6" class="text-center text-danger">加载失败，请重试</td></tr>';
        });

    bindListButtonEvents();
}

function bindListButtonEvents() {
    document.getElementById('recentIssuesContainer').addEventListener('click', function(e) {
        const target = e.target.closest('.view-issue');
        if (target) {
            const id = target.getAttribute('data-id');
            showDashIssueDetail(id);
            return;
        }
    });
}

/**
 * 格式化日期显示
 */
// function formatDate(dateArray) {
//     try {
//         // 检查是否为数组且长度至少为3（年、月、日）
//         if (!Array.isArray(dateArray) || dateArray.length < 3) {
//             throw new Error('Invalid date array');
//         }
//
//         const [year, month, day, hour = 0, minute = 0, second = 0] = dateArray;
//
//         // 创建日期对象
//         const date = new Date(year, month - 1, day, hour, minute, second); // 月份需要减1
//
//         // 检查日期是否有效
//         if (isNaN(date.getTime())) {
//             throw new Error('Invalid date');
//         }
//
//         // 格式化为 "YYYY-MM-DD HH:mm:ss"
//         const formattedYear = date.getFullYear();
//         const formattedMonth = String(date.getMonth() + 1).padStart(2, '0');
//         const formattedDay = String(date.getDate()).padStart(2, '0');
//         const formattedHours = String(date.getHours()).padStart(2, '0');
//         const formattedMinutes = String(date.getMinutes()).padStart(2, '0');
//         const formattedSeconds = String(date.getSeconds()).padStart(2, '0');
//
//         return `${formattedYear}-${formattedMonth}-${formattedDay} ${formattedHours}:${formattedMinutes}:${formattedSeconds}`;
//     } catch (error) {
//         console.error('数组日期格式化错误:', error);
//         // return JSON.stringify(dateArray); // 如果格式化失败，返回原始数组的字符串表示
//         return "--";
//     }
// }

function parseDateArray(dateArr) {
    // 校验：必须是数组且长度≥6，数值为合法数字
    if (!Array.isArray(dateArr) || dateArr.length < 3) {
        return new Date();
    }
    // 解构日期数组：年、月（减1）、日、时、分、秒
    const [year, month, day, hour, minute] = dateArr.map(item => Number(item));
    // 校验数值合法性
    if (isNaN(year) || isNaN(month) || isNaN(day) || isNaN(hour) || isNaN(minute)) {
        return new Date();
    }
    // JavaScript Date月份是0-11，后端传的是1-12，需减1
    const jsMonth = month - 1;
    // 构造Date对象（自动处理超出范围的数值，如13月会自动进位）
    const date = new Date(year, jsMonth, day, hour, minute);
    // 最终校验：确保Date对象合法
    return isNaN(date.getTime()) ? new Date() : date;
}

function jumpIssues() {
    window.app.loadModule('issues');
}

/**
 * 渲染近期事务排程甘特图
 */
function loadGanttChart() {
    fetch(`/api/dashboard/gantt?userId=${window.currentUser}`)
        .then(response => {
            if (!response.ok) throw new Error('获取排程数据失败');
            return response.json();
        })
        .then(rawData => {
            // 步骤1：数据清洗 - 过滤无效数据
            const validData = rawData.filter(item => {
                // 必须包含核心字段，且日期数组非空
                return item && item.issueIndex && item.projectName &&
                    Array.isArray(item.startTime) && item.startTime.length >= 5 &&
                    Array.isArray(item.deadline) && item.deadline.length >= 5;
            });

            const categories = validData.length > 0
                ? [...new Set(validData.map(item => item.projectName))] // 去重
                : [];

            // 步骤2：处理日期数组+格式化甘特图数据
            const ganttData = validData.map(item => {
                // 解析日期数组（自动处理月份偏移+容错）
                const startDate = parseDateArray(item.startTime);
                const endDate = parseDateArray(item.deadline);

                // 确保结束日期 ≥ 开始日期
                const finalEndDate = endDate < startDate ? new Date(startDate.getTime() + 86400000) : endDate;

                const yIndex = categories.indexOf(item.projectName);

                return {
                    id: `task-${item.issueIndex}`,
                    name: item.description, // 名称兜底
                    start: formatDate(item.startTime), // 转Highcharts所需的ISO格式
                    end: formatDate(item.deadline),
                    y: yIndex, // 按项目分组（y轴）
                    issueId: item.issueIndex, // 自定义字段：事务ID
                    color: getColorByStatus(item.issueStatusName) // 按状态配色
                };
            });

            // 步骤3：安全计算x轴范围（避免空数组报错）
            let xAxisMin, xAxisMax;
            if (ganttData.length > 0) {
                const allStartTimes = ganttData.map(item => new Date(item.start).getTime());
                const allEndTimes = ganttData.map(item => new Date(item.end).getTime());
                xAxisMin = Math.min(...allStartTimes) - 86400000; // 左偏移1天
                xAxisMax = Math.max(...allEndTimes) + 86400000; // 右偏移1天
            } else {
                // 无数据时，x轴范围为「今天-7天」到「今天+7天」
                const today = new Date().getTime();
                xAxisMin = today - 7 * 86400000;
                xAxisMax = today + 7 * 86400000;
            }

            // 步骤4：初始化Highcharts甘特图（确保无非法数值）
            Highcharts.ganttChart('ganttChart', {
                tooltip: {
                    formatter: function() {
                        const task = this.point;
                        const start = (new Date(task.start)).toLocaleDateString();
                        const end = new Date(task.end).toLocaleDateString();
                        return `<strong>${task.name}</strong><br/>
                                开始：${start}<br/>
                                结束：${end}`;
                    }
                },
                xAxis: {
                    type: 'datetime',
                    // 2. 区分主轴/次轴标签样式（核心：useHTML + formatter 识别刻度层级）
                    labels: {
                        // 基础样式（次轴默认样式，主轴通过HTML覆盖）
                        style: {
                            color: '#e9ecef',
                            fontSize: '12px',
                            fontWeight: '500'
                        },
                        // 调整标签间距，避免重叠
                        padding: 8,
                        rotation: 0 // 关闭旋转，提升可读性
                    },
                    // 3. 主轴/次轴轴线/刻度样式区分
                    lineColor: '#dee2e6', // 主轴轴线颜色（年份层级）
                    tickColor: '#e9ecef', // 次轴刻度颜色（月日层级）
                    tickLength: 8, // 主轴刻度长度
                    minorTickLength: 4, // 次轴刻度长度
                    minorTickColor: '#e2e8f0', // 次轴刻度颜色
                    min: xAxisMin,
                    max: xAxisMax,
                    minRange: 86400000,
                    currentDateIndicator: {
                        width: 1,
                        dashStyle: 'dot',
                        color: 'red',
                        label: {
                            format: '%Y-%m-%d'
                        }
                    }
                },
                yAxis: {
                    type: 'category',
                    categories: categories,
                    grid: {
                        enabled: true,
                        color: '#e9ecef'
                    },
                    labels: {
                        style: {
                            fontSize: '12px',
                            color: '#e2e8f0', // Y轴标签主色（项目名，高对比）
                            fontWeight: '500'
                        }
                    },
                    lineColor: '#e9ecef',
                    tickColor: '#e9ecef',
                    title: {
                        style: { color: '#e2e8f0' } // 若开启Y轴标题，配置颜色
                    }
                },
                series: [{
                    name: '事务排程',
                    data: ganttData,
                    borderColor: '#fff',
                    borderRadius: 4,
                    borderWidth: 1,
                    visible: ganttData.length > 0,
                    // 系列标签字体（若显示任务名）
                    dataLabels: {
                        enabled: false, // 可按需开启，开启后配置颜色
                        style: { color: '#e2e8f0' }
                    },
                    events: {
                        click: function(e) {
                            const point = e.point;
                            if (point && point.issueId) {
                                showDashIssueDetail(point.issueId); // 点击打开详情弹窗
                            }
                        }
                    }
                }],
                chart: {
                    backgroundColor: 'transparent',
                    plotBackgroundColor: 'transparent', // 绘图区背景透明
                    borderWidth: 0,
                    zooming: {
                        type: "x"
                    }
                },
                responsive: {
                    rules: [{
                        condition: { maxWidth: 768 },
                        chartOptions: {
                            yAxis: { labels: { fontSize: '11px', color: '#212529' } },
                            xAxis: { labels: { fontSize: '11px', color: '#495057' } },
                            tooltip: { style: { fontSize: '11px' } }
                        }
                    }]
                },
                // 空数据友好提示
                lang: { noData: '暂无排程数据' },
                noData: {
                    style: { fontSize: '16px', color: '#6c757d', textAlign: 'center' }
                }
            });
        })
        .catch(error => {
            console.error('渲染甘特图失败:', error);
            document.getElementById('ganttChart').innerHTML = '<div class="text-center text-danger">甘特图加载失败</div>';
        });
}

/**
 * 根据状态获取甘特图任务颜色
 */
function getColorByStatus(status) {
    const colorMap = {
        'OPEN': '#d63384',
        'IN PROGRESS': '#ffc107',
        'CLOSED': '#198754',
        'VALIDATION': '#20c997',
        'PENDING': '#6c757d'
    };
    return colorMap[status] || '#6c757d';
}

function showDashIssueDetail(issueId) {
    fetch(`/api/issues/${issueId}`)
        .then(response => {
            if (!response.ok) throw new Error('获取事务详情失败');
            return response.json();
        })
        .then(issue => {
            currentDashboardIssue = issue;

            const description = currentUserLang.includes('zh') ? issue.description_cn : currentUserLang.includes('en') ? issue.description_en : issue.description;
            document.getElementById('thisactionId').value = issue.issueIndex;
            document.getElementById('viewProjectName').textContent = issue.projectName || '未设置';
            document.getElementById('viewDescription').textContent = description || '无描述';
            document.getElementById('viewReporter').textContent = issue.issueReporterName || '未设置';
            document.getElementById('viewLeader').textContent = issue.issueOwnerName || '未设置';
            document.getElementById('viewExecutor').textContent = issue.actionOwnerName || '未设置';
            document.getElementById('viewExpectedCompletionTime').textContent = formatDate(issue.deadline);
            // 处理状态显示
            const statusMap = {
                'OPEN': '开启',
                'IN PROGRESS': '进行中',
                'CLOSED': '已关闭',
                'VALIDATION': '待验证',
                'PENDING': '待提交'
            };
            document.getElementById('viewStatus').textContent = statusMap[issue.issueStatusName] || issue.issueStatusName;

            // 处理更新记录
            const updateListContainer = document.getElementById('updateListContainer');
            if (issue.updateList && issue.updateList.length > 0) {
                // 按时间排序，最新的在前面
                const sortedUpdates = [...issue.updateList].sort((a, b) =>
                    new Date(b.updateTime).getTime() - new Date(a.updateTime).getTime()
                );

                let updatesHtml = '<div class="list-group">';
                sortedUpdates.forEach(update => {
                    const updateDesc = currentUserLang.includes('zh') ? update.update_cn : currentUserLang.includes('en') ? update.update_en : update.statusUpdate;
                    updatesHtml += `
                        <div class="list-group-item">
                            <div class="d-flex w-100 justify-content-between">
                                <h6 class="mb-1">${update.updateOwner.lastname.concat("",update.updateOwner.firstname) || '未知用户'}</h6>
                                <small>${formatDate(update.updateTime)}</small>
                            </div>
                            <p class="mb-1">${updateDesc || '无更新内容'}</p>
                        </div>
                    `;
                });
                updatesHtml += '</div>';
                updateListContainer.innerHTML = updatesHtml;
            } else {
                updateListContainer.innerHTML = '<p class="text-muted translate-item">暂无更新记录</p>';
            }

            openModal('viewDashTransactionModal');
        })
        .catch(error => {
            alert('获取详情失败: ' + error.message);
        });
}

/**
 * 子页面专属：初始化添加更新相关事件（子页面加载后调用）
 */
function initDashUpdateEvents() {
    // 获取按钮元素（子页面 DOM 已加载，可直接获取）
    const openBtn = document.getElementById('openDashAddUpdateBtn');
    const saveBtn = document.getElementById('saveDashUpdateBtn');

    if (!openBtn || !saveBtn) {
        console.error('子页面未找到添加/保存更新按钮');
        return;
    }

    // 3. 先解绑原有事件（避免重复进入子页面导致多次绑定）
    openBtn.removeEventListener('click', handleDashOpenUpdateModal);
    saveBtn.removeEventListener('click', handleDashSaveUpdate);

    // 4. 重新绑定事件
    openBtn.addEventListener('click', handleDashOpenUpdateModal);
    saveBtn.addEventListener('click', handleDashSaveUpdate);
}

/**
 * 点击「添加事务更新」按钮的处理函数（独立抽离，方便解绑）
 */
function handleDashOpenUpdateModal() {
    try {
        if (!currentDashboardIssue) {
            alert('请先选择有效的事务');
            return;
        }

        // 填充表单默认值
        document.getElementById('currentIssueId').value = currentDashboardIssue.issueIndex;
        document.getElementById('updateExecutor_dash').value = currentDashboardIssue.actionOwnerIndex || '';
        document.getElementById('updateLeader_dash').value = currentDashboardIssue.issueOwnerIndex || '';
        document.getElementById('updateStatus').value = currentDashboardIssue.issueStatusIndex || '';
        document.getElementById('updateContent').value = '';

        // 打开模态框
        openModal('addDashUpdateModal');
    } catch (error) {
        console.error('打开更新模态框失败：', error);
        alert('打开失败：' + error.message, 'error');
    }
}

/**
 * 点击「保存更新」按钮的处理函数（独立抽离，方便解绑）
 */
function handleDashSaveUpdate() {
    // 表单验证
    const updateContent = document.getElementById('updateContent').value.trim();
    if (!updateContent) {
        alert('更新内容不能为空', 'warning');
        return;
    }

    // 收集数据
    const updateData = {
        issueUpdate: {
            issue: {
                issueIndex: document.getElementById('currentIssueId').value
            },
            updateOwner: {
                userIndex: window.currentUser
            },
            statusUpdate: updateContent
        },
        issueStatusIndex: document.getElementById('updateStatus').value,
        actionOwnerIndex: document.getElementById('updateExecutor_dash').value,
        issueOwnerIndex: document.getElementById('updateLeader_dash').value
    };

    // 提交请求（逻辑不变，保留原有错误处理）
    fetch('/api/issues/update', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updateData)
    })
        .then(response => {
            if (!response.ok) throw new Error('提交更新失败');
            return response.json();
        })
        .then(result => {
            if (result.updateIndex) {
                alert('更新添加成功');
                addDashUpdateModal.hide();
                showDashIssueDetail(result.issue.issueIndex); // 刷新详情
            } else {
                alert('更新失败: ' + (result.message || '未知错误'), 'error');
            }
        })
        .catch(error => {
            alert('网络错误: ' + error.message, 'error');
        });
}

/**
 * 通用清理重复模态框DOM方法（初始化/关闭时复用）
 * @param {string} modalId - 模态框ID
 */
function cleanupDuplicateModals(modalId) {
    // 获取所有同名模态框元素
    const modals = document.querySelectorAll(`#${modalId}`);
    if (modals.length <= 1) return;

    // 保留第一个，移除其余的重复DOM
    for (let i = 1; i < modals.length; i++) {
        const modal = modals[i];
        // 先销毁重复实例（避免内存泄漏）
        if (window.bootstrap && window.bootstrap.Modal.getInstance(modal)) {
            window.bootstrap.Modal.getInstance(modal).dispose();
        }
        // 彻底移除重复DOM节点
        modal.remove();
        console.warn(`清理重复的${modalId}模态框DOM，已移除第${i+1}个重复节点`);
    }
}

/**
 * 绑定模态框关闭事件（带防重复绑定+二次清理）
 * @param {string} modalId - 模态框ID
 */
function bindModalCloseEvent(modalId) {
    const modalEl = document.getElementById(modalId);
    if (!modalEl) return;

    // 先移除旧的关闭事件（防重复绑定）
    const oldCloseHandler = modalEl.dataset.closeHandler;
    if (oldCloseHandler && window[oldCloseHandler]) {
        modalEl.removeEventListener('hidden.bs.modal', window[oldCloseHandler]);
    }

    // 定义新的关闭事件处理函数
    const closeHandler = function() {
        // 关闭时二次清理重复DOM
        cleanupDuplicateModals(modalId);
        // 销毁实例并清空缓存
        if (modalInstances[modalId]) {
            modalInstances[modalId].dispose();
            modalInstances[modalId] = null;
        }
        // 移除事件监听（防重复触发）
        modalEl.removeEventListener('hidden.bs.modal', closeHandler);
        modalEl.dataset.closeHandler = '';
    };

    // 绑定新事件并记录处理器（用于后续移除）
    modalEl.addEventListener('hidden.bs.modal', closeHandler);
    modalEl.dataset.closeHandler = `modalCloseHandler_${modalId}`;
    window[`modalCloseHandler_${modalId}`] = closeHandler;
}

/**
 * 创建单实例模态框（初始化时清理重复DOM+单实例保证）
 * @param {string} modalId - 模态框ID
 * @returns {bootstrap.Modal|null} 模态框实例
 */
function createSingleInstanceModal(modalId) {
    // 初始化第一步：清理重复DOM
    cleanupDuplicateModals(modalId);

    const modalEl = document.getElementById(modalId);
    if (!modalEl) {
        console.error(`未找到${modalId}模态框DOM节点`);
        return null;
    }

    // 单实例保证：先销毁已有实例
    const oldInstance = window.bootstrap?.Modal.getInstance(modalEl);
    if (oldInstance) {
        oldInstance.dispose();
    }

    // 创建新实例并缓存
    const newInstance = new window.bootstrap.Modal(modalEl);
    modalInstances[modalId] = newInstance;

    // 绑定关闭事件（带防重复绑定）
    bindModalCloseEvent(modalId);

    return newInstance;
}

/**
 * 初始化所有模态框监听（入口方法）
 */
function initModalCloseListeners() {
    // 初始化viewDashTransactionModal（单实例+清理重复）
    if (document.getElementById('viewDashTransactionModal')) {
        createSingleInstanceModal('viewDashTransactionModal');
    }

    // 初始化addDashUpdateModal（单实例+清理重复）
    if (document.getElementById('addDashUpdateModal')) {
        createSingleInstanceModal('addDashUpdateModal');
    }
}

// 打开模态框的统一入口（推荐使用此方法打开，保证单实例）
function openModal(modalId) {
    const instance = createSingleInstanceModal(modalId);
    instance?.show();
}