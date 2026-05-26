/**
 * 初始化数据统计模块
 */
function initStatistics() {
    console.log('初始化数据统计模块');
    
    // 初始化统计筛选器
    initStatisticsFilters();
    
    // 初始化设备状态统计图表
    initDeviceStatusChart();
    
    // 初始化事件统计图表
    initEventStatisticsChart();
    
    // 初始化设备性能统计图表
    initDevicePerformanceChart();
    
    // 初始化设备分布地图
    initDeviceDistributionMap();
}

/**
 * 初始化统计筛选器
 */
function initStatisticsFilters() {
    const timeRangeSelect = document.getElementById('timeRangeSelect');
    if (!timeRangeSelect) {
        console.warn('未找到时间范围选择器');
        return;
    }
    
    // 绑定时间范围变更事件
    timeRangeSelect.addEventListener('change', function() {
        const timeRange = this.value;
        updateStatisticsByTimeRange(timeRange);
    });
}

/**
 * 根据时间范围更新统计数据
 * @param {string} timeRange - 时间范围 (day, week, month, year)
 */
function updateStatisticsByTimeRange(timeRange) {
    app.showToast(`更新 ${getTimeRangeText(timeRange)} 的统计数据`);
    
    // 更新所有图表
    initDeviceStatusChart();
    initEventStatisticsChart();
    initDevicePerformanceChart();
}

/**
 * 获取时间范围文本
 */
function getTimeRangeText(timeRange) {
    const texts = {
        'day': '今日',
        'week': '本周',
        'month': '本月',
        'quarter': '本季度',
        'year': '本年'
    };
    
    return texts[timeRange] || '所有时间';
}

/**
 * 初始化设备状态统计图表
 */
function initDeviceStatusChart() {
    const chartDom = document.getElementById('deviceStatusChart');
    if (!chartDom) {
        console.warn('未找到设备状态统计图表容器');
        return;
    }
    
    // 清除已有图表实例
    if (chartDom.__chartInstance) {
        chartDom.__chartInstance.dispose();
    }
    
    const myChart = echarts.init(chartDom);
    chartDom.__chartInstance = myChart;
    
    // 获取时间范围
    const timeRangeSelect = document.getElementById('timeRangeSelect');
    const timeRange = timeRangeSelect ? timeRangeSelect.value : 'month';
    
    // 根据时间范围生成数据
    const { xAxisData, onlineData, offlineData, warningData } = generateDeviceStatusData(timeRange);
    
    // 图表配置
    const option = {
        title: {
            text: '设备状态趋势',
            left: 'center'
        },
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'shadow'
            }
        },
        legend: {
            data: ['在线设备', '离线设备', '警告设备'],
            top: 30
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis: {
            type: 'category',
            data: xAxisData
        },
        yAxis: {
            type: 'value',
            name: '设备数量'
        },
        series: [
            {
                name: '在线设备',
                type: 'bar',
                data: onlineData,
                itemStyle: {
                    color: '#28a745'
                }
            },
            {
                name: '离线设备',
                type: 'bar',
                data: offlineData,
                itemStyle: {
                    color: '#dc3545'
                }
            },
            {
                name: '警告设备',
                type: 'bar',
                data: warningData,
                itemStyle: {
                    color: '#ffc107'
                }
            }
        ]
    };
    
    myChart.setOption(option);
    
    // 响应窗口大小变化
    window.addEventListener('resize', () => {
        myChart.resize();
    });
}

/**
 * 生成设备状态数据
 */
function generateDeviceStatusData(timeRange) {
    let xAxisData = [];
    let onlineData = [];
    let offlineData = [];
    let warningData = [];
    const totalDevices = 128;
    
    switch(timeRange) {
        case 'day':
            // 今日数据（每小时）
            for (let i = 0; i < 24; i++) {
                xAxisData.push(`${i}:00`);
                const online = Math.floor(Math.random() * 20) + (totalDevices - 30);
                const offline = Math.floor(Math.random() * 10) + 5;
                const warning = totalDevices - online - offline;
                
                onlineData.push(online);
                offlineData.push(offline);
                warningData.push(warning);
            }
            break;
        case 'week':
            // 本周数据（每天）
            const weekdays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
            weekdays.forEach(day => {
                xAxisData.push(day);
                const online = Math.floor(Math.random() * 20) + (totalDevices - 30);
                const offline = Math.floor(Math.random() * 10) + 5;
                const warning = totalDevices - online - offline;
                
                onlineData.push(online);
                offlineData.push(offline);
                warningData.push(warning);
            });
            break;
        case 'month':
            // 本月数据（每周）
            for (let i = 1; i <= 4; i++) {
                xAxisData.push(`第${i}周`);
                const online = Math.floor(Math.random() * 15) + (totalDevices - 25);
                const offline = Math.floor(Math.random() * 10) + 5;
                const warning = totalDevices - online - offline;
                
                onlineData.push(online);
                offlineData.push(offline);
                warningData.push(warning);
            }
            break;
        case 'quarter':
            // 本季度数据（每月）
            const months = ['1月', '2月', '3月'];
            months.forEach(month => {
                xAxisData.push(month);
                const online = Math.floor(Math.random() * 15) + (totalDevices - 25);
                const offline = Math.floor(Math.random() * 10) + 5;
                const warning = totalDevices - online - offline;
                
                onlineData.push(online);
                offlineData.push(offline);
                warningData.push(warning);
            });
            break;
        case 'year':
            // 本年数据（每季度）
            for (let i = 1; i <= 4; i++) {
                xAxisData.push(`第${i}季度`);
                const online = Math.floor(Math.random() * 15) + (totalDevices - 25);
                const offline = Math.floor(Math.random() * 10) + 5;
                const warning = totalDevices - online - offline;
                
                onlineData.push(online);
                offlineData.push(offline);
                warningData.push(warning);
            }
            break;
    }
    
    return { xAxisData, onlineData, offlineData, warningData };
}

/**
 * 初始化事件统计图表
 */
function initEventStatisticsChart() {
    const chartDom = document.getElementById('eventStatisticsChart');
    if (!chartDom) {
        console.warn('未找到事件统计图表容器');
        return;
    }
    
    // 清除已有图表实例
    if (chartDom.__chartInstance) {
        chartDom.__chartInstance.dispose();
    }
    
    const myChart = echarts.init(chartDom);
    chartDom.__chartInstance = myChart;
    
    // 获取时间范围
    const timeRangeSelect = document.getElementById('timeRangeSelect');
    const timeRange = timeRangeSelect ? timeRangeSelect.value : 'month';
    
    // 根据时间范围生成数据
    const { xAxisData, criticalData, warningData, infoData } = generateEventStatisticsData(timeRange);
    
    // 图表配置
    const option = {
        title: {
            text: '事件类型统计',
            left: 'center'
        },
        tooltip: {
            trigger: 'axis'
        },
        legend: {
            data: ['严重事件', '警告事件', '信息事件'],
            top: 30
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis: {
            type: 'category',
            boundaryGap: false,
            data: xAxisData
        },
        yAxis: {
            type: 'value',
            name: '事件数量'
        },
        series: [
            {
                name: '严重事件',
                type: 'line',
                stack: 'total',
                data: criticalData,
                lineStyle: {
                    color: '#dc3545'
                },
                itemStyle: {
                    color: '#dc3545'
                }
            },
            {
                name: '警告事件',
                type: 'line',
                stack: 'total',
                data: warningData,
                lineStyle: {
                    color: '#ffc107'
                },
                itemStyle: {
                    color: '#ffc107'
                }
            },
            {
                name: '信息事件',
                type: 'line',
                stack: 'total',
                data: infoData,
                lineStyle: {
                    color: '#17a2b8'
                },
                itemStyle: {
                    color: '#17a2b8'
                }
            }
        ]
    };
    
    myChart.setOption(option);
    
    // 响应窗口大小变化
    window.addEventListener('resize', () => {
        myChart.resize();
    });
}

/**
 * 生成事件统计数据
 */
function generateEventStatisticsData(timeRange) {
    let xAxisData = [];
    let criticalData = [];
    let warningData = [];
    let infoData = [];
    
    switch(timeRange) {
        case 'day':
            // 今日数据（每小时）
            for (let i = 0; i < 24; i++) {
                xAxisData.push(`${i}:00`);
                criticalData.push(Math.floor(Math.random() * 3));
                warningData.push(Math.floor(Math.random() * 8) + 2);
                infoData.push(Math.floor(Math.random() * 15) + 5);
            }
            break;
        case 'week':
            // 本周数据（每天）
            const weekdays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
            weekdays.forEach(day => {
                xAxisData.push(day);
                criticalData.push(Math.floor(Math.random() * 10) + 2);
                warningData.push(Math.floor(Math.random() * 30) + 10);
                infoData.push(Math.floor(Math.random() * 50) + 20);
            });
            break;
        case 'month':
            // 本月数据（每周）
            for (let i = 1; i <= 4; i++) {
                xAxisData.push(`第${i}周`);
                criticalData.push(Math.floor(Math.random() * 20) + 5);
                warningData.push(Math.floor(Math.random() * 80) + 30);
                infoData.push(Math.floor(Math.random() * 150) + 70);
            }
            break;
        case 'quarter':
            // 本季度数据（每月）
            const months = ['1月', '2月', '3月'];
            months.forEach(month => {
                xAxisData.push(month);
                criticalData.push(Math.floor(Math.random() * 50) + 10);
                warningData.push(Math.floor(Math.random() * 200) + 50);
                infoData.push(Math.floor(Math.random() * 400) + 100);
            });
            break;
        case 'year':
            // 本年数据（每季度）
            for (let i = 1; i <= 4; i++) {
                xAxisData.push(`第${i}季度`);
                criticalData.push(Math.floor(Math.random() * 100) + 30);
                warningData.push(Math.floor(Math.random() * 500) + 100);
                infoData.push(Math.floor(Math.random() * 1000) + 300);
            }
            break;
    }
    
    return { xAxisData, criticalData, warningData, infoData };
}

/**
 * 初始化设备性能统计图表
 */
function initDevicePerformanceChart() {
    const chartDom = document.getElementById('devicePerformanceChart');
    if (!chartDom) {
        console.warn('未找到设备性能统计图表容器');
        return;
    }
    
    // 清除已有图表实例
    if (chartDom.__chartInstance) {
        chartDom.__chartInstance.dispose();
    }
    
    const myChart = echarts.init(chartDom);
    chartDom.__chartInstance = myChart;
    
    // 图表配置
    const option = {
        title: {
            text: '设备类型性能对比',
            left: 'center'
        },
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'shadow'
            }
        },
        legend: {
            data: ['平均响应时间(ms)', '故障率(%)', '数据准确率(%)'],
            top: 30
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis: {
            type: 'value'
        },
        yAxis: {
            type: 'category',
            data: ['温度传感器', '湿度传感器', '压力传感器', '流量传感器', '控制设备', '网关设备']
        },
        series: [
            {
                name: '平均响应时间(ms)',
                type: 'bar',
                data: [12, 15, 18, 22, 35, 28],
                itemStyle: {
                    color: '#6c757d'
                }
            },
            {
                name: '故障率(%)',
                type: 'bar',
                data: [2.5, 3.2, 4.1, 5.3, 1.8, 3.5],
                itemStyle: {
                    color: '#dc3545'
                }
            },
            {
                name: '数据准确率(%)',
                type: 'bar',
                data: [98.2, 97.5, 96.8, 95.7, 99.1, 98.5],
                itemStyle: {
                    color: '#28a745'
                }
            }
        ]
    };
    
    myChart.setOption(option);
    
    // 响应窗口大小变化
    window.addEventListener('resize', () => {
        myChart.resize();
    });
}

/**
 * 初始化设备分布地图
 */
function initDeviceDistributionMap() {
    const chartDom = document.getElementById('deviceDistributionMap');
    if (!chartDom) {
        console.warn('未找到设备分布地图容器');
        return;
    }
    
    // 清除已有图表实例
    if (chartDom.__chartInstance) {
        chartDom.__chartInstance.dispose();
    }
    
    const myChart = echarts.init(chartDom);
    chartDom.__chartInstance = myChart;
    
    // 图表配置
    const option = {
        title: {
            text: '设备区域分布',
            left: 'center'
        },
        tooltip: {
            trigger: 'item'
        },
        legend: {
            orient: 'vertical',
            left: 'left'
        },
        series: [
            {
                name: '设备数量',
                type: 'pie',
                radius: '70%',
                data: [
                    { value: 35, name: '车间A-1区' },
                    { value: 35, name: '车间A-1区' },
                    { value: 28, name: '车间A-2区' },
                    { value: 22, name: '车间B-1区' },
                    { value: 18, name: '车间B-2区' },
                    { value: 12, name: '控制室' },
                    { value: 13, name: '机房' }
                ],
                emphasis: {
                    itemStyle: {
                        shadowBlur: 10,
                        shadowOffsetX: 0,
                        shadowColor: 'rgba(0, 0, 0, 0.5)'
                    }
                }
            }
        ]
    };
    
    myChart.setOption(option);
    
    // 响应窗口大小变化
    window.addEventListener('resize', () => {
        myChart.resize();
    });
}
    