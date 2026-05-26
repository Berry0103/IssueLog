let currentSortField = 'startTime'; // 当前排序字段
let currentSortDirection = 'desc'; // 当前排序方向（asc/desc
let fullIssueList = [];
let currentIssueList = [];
// 定义模态框实例
let transactionModalInstance = null;
// 存储当前查看的事务信息（供添加更新使用）
let currentViewIssue = null;
let addUpdateModal = null;

// 新增：加载所有事务数据到 fullIssueList
function loadAllIssues() {
    filters = { page: 0, size: 99999 }
    let queryParams = new URLSearchParams();
    Object.keys(filters).forEach(key => {
        if (filters[key]) queryParams.append(key, filters[key]);
    });

    // 调用API获取事务列表（适配Page返回格式）
    fetch(`/api/issues?${queryParams.toString()}`)// 假设 pageSize 足够大以获取全部数据
        .then(response => {
            if (!response.ok) throw new Error('获取全量事务数据失败');
            return response.json();
        })
        .then(pageData => {
            fullIssueList = pageData.content || []; // 存储全量数据
            currentIssueList = [...fullIssueList]
            console.log('全量事务数据加载完成，共', fullIssueList.length, '条');
        })
        .catch(error => console.error('加载全量事务数据失败:', error));
}

// 修改 initIssueManagement，先加载全量数据再初始化列表
/**
 * 初始化事务管理模块
 */
function initIssueManagement() {
    console.log('初始化事务管理模块');
    loadAllIssues(); // 新增：先加载全量数据
    initIssueList({ page: 1, pageSize: 10 }); // 初始加载第一页
    initIssueFilters();
    initIssueActionButtons();
    // 新增：初始化排序功能
    initTableSort();
    initIssueUpdateEvents();
}

/**
 * 初始化表格排序功能
 */
function initTableSort() {
    // 获取所有可排序的表头
    const sortableHeaders = document.querySelectorAll('.sortable');

    sortableHeaders.forEach(header => {
        header.addEventListener('click', function() {
            // 获取当前点击列的排序字段（从 data-sort-field 属性）
            const targetField = this.getAttribute('data-sort-field');

            // 切换排序状态
            if (targetField === currentSortField) {
                // 同一字段：切换升序/降序
                currentSortDirection = currentSortDirection === 'asc' ? 'desc' : 'asc';
            } else {
                // 不同字段：默认升序
                currentSortField = targetField;
                currentSortDirection = 'asc';
            }

            // 更新表头UI状态
            updateSortHeaderUI();

            // 执行搜索+排序+渲染（复用现有逻辑）
            performSearch();
        });
    });

    // 初始加载时更新UI（默认排序字段高亮）
    updateSortHeaderUI();
}

/**
 * 更新排序表头UI状态（显示箭头和高亮）
 */
function updateSortHeaderUI() {
    // 移除所有表头的active和箭头类
    document.querySelectorAll('.sortable').forEach(header => {
        header.classList.remove('active');
        const icon = header.querySelector('.sort-icon');
        icon.classList.remove('asc', 'desc');
    });

    // 给当前排序字段添加active和对应箭头
    const currentHeader = document.querySelector(`.sortable[data-sort-field="${currentSortField}"]`);
    if (currentHeader) {
        currentHeader.classList.add('active');
        const icon = currentHeader.querySelector('.sort-icon');
        icon.classList.add(currentSortDirection);
    }
}

/**
 * 为下拉框添加搜索过滤功能
 * @param {string} searchInputId - 搜索输入框的ID
 * @param {string} selectElementId - 下拉框的ID
 */
function addSearchFunctionality(searchInputId, selectElementId) {
    const searchInput = document.getElementById(searchInputId);
    const selectElement = document.getElementById(selectElementId);

    if (!searchInput || !selectElement) return;

    // 保存原始选项列表，以便在搜索为空时恢复
    const originalOptions = Array.from(selectElement.options).map(option => ({
        value: option.value,
        text: option.text,
        element: option
    }));

    searchInput.addEventListener('input', function() {
        const searchTerm = this.value.toLowerCase().trim();

        // 清空当前下拉框（保留第一个默认选项，如“全部项目”）
        while (selectElement.options.length > 1) {
            selectElement.remove(1);
        }

        if (searchTerm === '') {
            // 如果搜索为空，恢复原始选项
            originalOptions.slice(1).forEach(optionData => {
                selectElement.appendChild(optionData.element.cloneNode(true));
            });
            return;
        }

        // 过滤并添加匹配的选项
        const filteredOptions = originalOptions.filter(optionData =>
            optionData.text.toLowerCase().includes(searchTerm)
        );

        filteredOptions.forEach(optionData => {
            // 只添加非默认选项
            if (optionData.value !== '') {
                selectElement.appendChild(optionData.element.cloneNode(true));
            }
        });
    });
}

/**
 * 执行搜索操作
 */
function performSearch() {
    const keyword = document.getElementById('globalSearchInput').value.trim();
    const currentPage = 1;
    const pageSize = 10;

    renderFilteredTable(keyword, currentPage, pageSize);

    // 计算筛选后的总数和总页数
    let filteredAndSortedCount;
    if (keyword === '') {
        filteredAndSortedCount = sortIssues([...fullIssueList], currentSortField, currentSortDirection).length;
    } else {
        // 先筛选，再排序，最后统计总数
        const filtered = fullIssueList.filter(issue => {
            // 筛选逻辑不变...
            const searchableString = (
                (issue.issueIndex || '') +
                (issue.projectName || '') +
                (issue.description || '') +
                (issue.actionOwnerName || '') +
                (issue.issueOwnerName || '') +
                (issue.issueStatusName || '') +
                (new Date(issue.startTime).toLocaleString() || '') +
                (new Date(issue.deadline).toLocaleString() || '')
            ).toLowerCase();
            return searchableString.includes(keyword.toLowerCase());
        });
        filteredAndSortedCount = sortIssues(filtered, currentSortField, currentSortDirection).length;
    }

    // 新增：计算总页数
    const totalPages = Math.ceil(filteredAndSortedCount / pageSize);
    // 传入 totalPages 参数
    updatePagination(filteredAndSortedCount, currentPage, pageSize, totalPages);
}

/**
 * 根据关键词筛选并渲染表格
 * @param {string} keyword 搜索关键词
 * @param {number} currentPage 当前页码 (1基)
 * @param {number} pageSize 每页条数
 */
function renderFilteredTable(keyword, currentPage, pageSize) {
    const tableBody = document.getElementById('transactionsTableBody');
    if (!tableBody) return;

    let filteredIssues = [...fullIssueList]; // 复制一份原始数据进行操作

    // 1. 根据关键词进行模糊筛选
    if (keyword && keyword.trim() !== '') {
        const lowerCaseKeyword = keyword.toLowerCase().trim();
        filteredIssues = filteredIssues.filter(issue => {
            // 将所有要匹配的字段转换为字符串并拼接，然后检查是否包含关键词
            const searchableString = (
                (issue.issueIndex || '') +
                (issue.projectName || '') +
                (issue.description || '') +
                (issue.actionOwnerName || '') +
                (issue.issueOwnerName || '') +
                (issue.issueStatusName || '') +
                (new Date(issue.startTime).toLocaleString() || '') +
                (new Date(issue.deadline).toLocaleString() || '')
            ).toLowerCase();
            return searchableString.includes(lowerCaseKeyword);
        });
    }

    // 步骤2：新增排序逻辑（筛选后、分页前执行）
    const sortedIssues = sortIssues(filteredIssues, currentSortField, currentSortDirection);

    // 3. 处理分页
    const startIndex = (currentPage - 1) * pageSize;
    const paginatedIssues = sortedIssues.slice(startIndex, startIndex + pageSize);

    // 4. 渲染表格
    let html = '';
    if (paginatedIssues.length === 0) {
        html = '<tr><td colspan="9" class="text-center">暂无匹配的数据</td></tr>';
    } else {
        paginatedIssues.forEach(issue => {
            const statusMap = {
                'OPEN': { class: 'column-open', text: '开启' },
                'IN PROGRESS': { class: 'column-progress', text: '进行中' },
                'CLOSED': { class: 'bg-success', text: '已关闭' },
                'VALIDATION': { class: 'column-validation', text: '待验证' },
                'PENDING': { class: 'column-pending', text: '待提交' }
            };
            const priorityMap = {
                'HIGH': { class: 'bs-danger', text: '高' },
                'MID': { class: 'bs-warning', text: '中' },
                'LOW': { class: 'bg-success', text: '低' }
            };

            const status = statusMap[issue.issueStatusName] || { class: 'bg-secondary', text: issue.issueStatusName };
            const priority = priorityMap[issue.priorityName] || { class: 'bg-success', text: issue.priorityName };
            const description = currentUserLang.includes('zh') ? issue.description_cn : currentUserLang.includes('en') ? issue.description_en : issue.description;

            html += `
                <tr>
                    <td class="d-none">${issue.issueIndex}</td>
                    <td data-sort-field="projectName">${issue.projectName} <span class="sort-icon"></span></td>
                    <td>${description}</td>
                    <td class="text-center"><span class="badge ${priority.class}">${priority.text}</span></td>
                    <td data-sort-field="actionOwnerName">${issue.actionOwnerName} <span class="sort-icon"></span></td>
                    <td data-sort-field="issueOwnerName">${issue.issueOwnerName} <span class="sort-icon"></span></td>
                    <td class="text-center"><span class="badge ${status.class}">${status.text}</span></td>
                    <td class="text-center" data-sort-field="startTime">${formatDate(issue.startTime)} <span class="sort-icon"></span></td>
                    <td class="text-center">${formatDate(issue.deadline)}</td>
                    <td class="text-center">${formatDate(issue.lastUpdateTime)}</td>
                    <td class="text-center">
                        <div class="btn-group btn-group-sm">
                            <button class="btn btn-primary view-issue" data-id="${issue.issueIndex}">查看</button>
                            <button class="btn btn-secondary edit-issue" data-id="${issue.issueIndex}">编辑</button>
<!--                            <button class="btn btn-danger delete-issue" data-id="${issue.issueIndex}">删除</button>-->
                        </div>
                    </td>
                </tr>
            `;
        });
    }
    tableBody.innerHTML = html;

    // 绑定行操作事件
    bindIssueActionEvents();
}

/**
 * 事务列表排序核心函数
 * @param {Array} issues 待排序的事务数组
 * @param {string} field 排序字段
 * @param {string} direction 排序方向（asc/desc）
 * @returns {Array} 排序后的数组
 */
function sortIssues(issues, field, direction) {
    const sorted = [...issues]; // 避免修改原数组

    sorted.sort((a, b) => {
        let valueA = a[field] || ''; // 获取字段值，默认空字符串
        let valueB = b[field] || '';
        let compareResult = 0; // 存储基础比较结果

        // 处理不同类型字段的排序逻辑
        switch (field) {
            // 日期类型：转成时间戳比较
            case 'startTime':
                valueA = new Date(valueA).getTime();
                valueB = new Date(valueB).getTime();
                break;
            // 字符串类型：支持中文排序
            case 'projectName':
            case 'actionOwnerName':
            case 'issueOwnerName':
                compareResult = valueA.localeCompare(valueB, 'zh-Hans-CN');
                break;
            // 其他类型（如数字）：直接比较
            default:
                compareResult = valueA > valueB ? 1 : (valueA < valueB ? -1 : 0);
                break;
        }

        // 根据排序方向返回比较结果
        return direction === 'asc' ? compareResult : -compareResult;
    });

    return sorted;
}

/**
 * 初始化事务列表
 * @param {Object} filters 筛选条件，包含分页参数(page:1基, pageSize)
 */
function initIssueList(filters = { page: 1, size: 10 }) {
    const tableBody = document.getElementById('transactionsTableBody');
    if (!tableBody) return;

    // 显示加载状态
    tableBody.innerHTML = '<tr><td colspan="9" class="text-center">加载中...</td></tr>';

    // 构建查询参数
    let queryParams = new URLSearchParams();
    Object.keys(filters).forEach(key => {
        if (filters[key]) queryParams.append(key, filters[key]);
    });

    // 添加当前排序字段和方向到查询参数
    queryParams.append('sortField', currentSortField);
    queryParams.append('sortDirection', currentSortDirection);

    // 调用API获取事务列表（适配Page返回格式）
    fetch(`/api/issues?${queryParams.toString()}`)
        .then(response => {
            if (!response.ok) throw new Error('获取事务列表失败');
            return response.json();
        })
        .then(pageData => {
            console.log(pageData)
            // Page对象结构：{content: [], totalElements: 0, number: 0, size: 10, totalPages: 0}
            const issues = pageData.content; // 数据列表在content属性中
            const total = pageData.totalElements; // 总条数
            const currentPage = pageData.number + 1; // 转换为1基页码
            const pageSize = pageData.size; // 每页条数
            const totalPages = pageData.totalPages; // 总页数

            let html = '';

            if (issues.length === 0) {
                html = '<tr><td colspan="9" class="text-center">暂无事务数据</td></tr>';
            } else {
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

                    html += `
                        <tr>
                            <td class="d-none">${issue.issueIndex}</td>
                            <td data-sort-field="projectName">${issue.projectName} <span class="sort-icon"></span></td>
                            <td>${description}</td>
                            <td class="text-center"><span class="badge ${priority.class}">${priority.text}</span></td>
                            <td data-sort-field="actionOwnerName">${issue.actionOwnerName} <span class="sort-icon"></span></td>
                            <td data-sort-field="issueOwnerName">${issue.issueOwnerName} <span class="sort-icon"></span></td>
                            <td class="text-center"><span class="badge ${status.class}">${status.text}</span></td>
                            <td class="text-center" data-sort-field="startTime">${formatDate(issue.startTime)} <span class="sort-icon"></span></td>
                            <td class="text-center">${formatDate(issue.deadline)}</td>
                            <td class="text-center">${formatDate(issue.lastUpdateTime)}</td>
                            <td class="text-center">
                                <div class="btn-group btn-group-sm">
                                    <button class="btn btn-primary view-issue" data-id="${issue.issueIndex}">查看</button>
                                    <button class="btn btn-secondary edit-issue" data-id="${issue.issueIndex}">编辑</button>
<!--                                    <button class="btn btn-danger delete-issue" data-id="${issue.issueIndex}">删除</button>-->
                                </div>
                            </td>
                        </tr>
                    `;
                });
            }

            tableBody.innerHTML = html;
            // 绑定行操作事件
            bindIssueActionEvents();
            // 更新分页（使用Page对象提供的总页数）
            updatePagination(total, currentPage, pageSize, totalPages);
        })
        .catch(error => {
            console.error('加载事务列表失败:', error);
            tableBody.innerHTML = '<tr><td colspan="9" class="text-center text-danger">加载失败，请重试</td></tr>';
        });
}

/**
 * 初始化筛选器
 */
function initIssueFilters() {
    // 保留全局搜索和日期筛选功能
    const globalSearchInput = document.getElementById('globalSearchInput');
    const startDateFilter = document.getElementById('startDateFilter');
    const endDateFilter = document.getElementById('endDateFilter');

    // 全局搜索输入事件
    globalSearchInput.addEventListener('input', () => {
        performSearch();
    });

    // 日期变化事件
    startDateFilter.addEventListener('change', () => {
        initIssueList({
            startDate: startDateFilter.value,
            endDate: endDateFilter.value,
            page: 1,
            pageSize: 10
        });
    });

    endDateFilter.addEventListener('change', () => {
        initIssueList({
            startDate: startDateFilter.value,
            endDate: endDateFilter.value,
            page: 1,
            pageSize: 10
        });
    });

    // 保留原有筛选器功能
    loadProjects();
    loadExecutors();

    // 绑定筛选表单提交事件
    const filterForm = document.getElementById('transactionFilterForm');
    if (filterForm) {
        // 表单提交处理逻辑
        const handleFilterSubmit = function(e) {
            e.preventDefault();

            const currentPage = document.querySelector('#pagination .page-item.active .page-link')?.getAttribute('data-page') || 1;
            const filters = {
                projectIndex: document.getElementById('projectFilter').value,
                issueStatus: document.getElementById('statusFilter').value,
                actionOwner: document.getElementById('executorFilter').value,
                startTime: document.getElementById('startDateFilter').value,
                deadline: document.getElementById('endDateFilter').value,
                page: parseInt(currentPage),
                pageSize: 10
            };

            initIssueList(filters);
        };

        // 绑定表单提交事件
        filterForm.addEventListener('submit', handleFilterSubmit);

        // 为下拉框添加change事件，触发表单提交
        const filterSelects = [
            document.getElementById('projectFilter'),
            document.getElementById('statusFilter'),
            document.getElementById('executorFilter')
        ];

        filterSelects.forEach(select => {
            if (select) {
                select.addEventListener('change', () => {
                    // 手动触发表单提交事件
                    filterForm.dispatchEvent(new Event('submit'));
                });
            }
        });
    }
}

/**
 * 加载项目列表到筛选器和模态框，并排序
 */
function loadProjects() {
    const projectFilter = document.getElementById('projectFilter');
    const projectSelect = document.getElementById('projectSelect');

    if (!projectFilter || !projectSelect) return;

    fetch('/api/project/projects')
        .then(response => response.json())
        .then(projects => {
            // 按项目名称排序
            const sortedProjects = [...projects].sort((a, b) => {
                return a.projectName.localeCompare(b.projectName, 'zh-CN');
            });

            // 清空现有选项（保留第一个"全部"或"请选择"选项）
            while (projectFilter.options.length > 1) {
                projectFilter.remove(1);
            }
            while (projectSelect.options.length > 1) {
                projectSelect.remove(1);
            }

            // 用于存储原始选项，供搜索功能使用
            const projectOptionsData = [];

            sortedProjects.forEach(project => {
                const optionText = project.projectName;
                const optionValue = project.projectIndex;

                // 存储选项数据
                projectOptionsData.push({ text: optionText, value: optionValue });

                // 为筛选器添加选项
                const filterOption = document.createElement('option');
                filterOption.value = optionValue;
                filterOption.textContent = optionText;
                projectFilter.appendChild(filterOption);

                // 为模态框添加选项
                const selectOption = document.createElement('option');
                selectOption.value = optionValue;
                selectOption.textContent = optionText;
                projectSelect.appendChild(selectOption);
            });

            // 为两个下拉框分别添加搜索功能
            addSearchFunctionality('projectSearchInput', 'projectFilter');
            addSearchFunctionality('modalProjectSearchInput', 'projectSelect');

        })
        .catch(error => console.error('加载项目列表失败:', error));
}

/**
 * 加载执行人列表到筛选器和模态框，并排序
 */
function loadExecutors() {
    const executorFilter = document.getElementById('executorFilter');
    const executorSelect = document.getElementById('executorSelect');
    const leaderSelect = document.getElementById('leaderSelect');
    const updateExecutor = document.getElementById('updateExecutor');
    const updateLeader = document.getElementById('updateLeader');

    if (!executorFilter || !executorSelect || !leaderSelect || !updateExecutor || !updateLeader) return;

    fetch('/api/user/userlist')
        .then(response => response.json())
        .then(users => {
            // 按用户姓名排序
            const sortedUsers = [...users].sort((a, b) => {
                return a.fullName.localeCompare(b.fullName, 'zh-CN');
            });

            // 清空现有选项（保留第一个"全部"或"请选择"选项）
            while (executorFilter.options.length > 1) {
                executorFilter.remove(1);
            }
            while (executorSelect.options.length > 1) {
                executorSelect.remove(1);
            }
            while (leaderSelect.options.length > 1) {
                leaderSelect.remove(1);
            }

            // 用于存储原始选项，供搜索功能使用
            const userOptionsData = [];

            sortedUsers.forEach(user => {
                const optionText = user.fullName;
                const optionValue = user.userIndex;

                // 存储选项数据
                userOptionsData.push({ text: optionText, value: optionValue });

                // 为筛选器添加选项
                const filterOption = document.createElement('option');
                filterOption.value = optionValue;
                filterOption.textContent = optionText;
                executorFilter.appendChild(filterOption);

                // 为模态框添加选项
                const selectOption = document.createElement('option');
                selectOption.value = optionValue;
                selectOption.textContent = optionText;
                executorSelect.appendChild(selectOption);
                const selectOption3 = document.createElement('option');
                selectOption3.value = optionValue;
                selectOption3.textContent = optionText;
                leaderSelect.appendChild(selectOption3);

                const selectOption1 = document.createElement('option');
                selectOption1.value = optionValue;
                selectOption1.textContent = optionText;
                updateExecutor.appendChild(selectOption1);

                const selectOption2 = document.createElement('option');
                selectOption2.value = optionValue;
                selectOption2.textContent = optionText;
                updateLeader.appendChild(selectOption2);
            });

            // 为两个下拉框分别添加搜索功能
            addSearchFunctionality('executorSearchInput', 'executorFilter');
            addSearchFunctionality('modalExecutorSearchInput', 'executorSelect');
            addSearchFunctionality('负责人Input', 'leaderSelect');

        })
        .catch(error => console.error('加载执行人列表失败:', error));
}


/**
 * 初始化操作按钮事件
 */
function initIssueActionButtons() {
    // 新建事务按钮
    const createBtn = document.getElementById('createTransactionBtn');
    if (createBtn) {
        createBtn.addEventListener('click', () => {
            // 复用同一个实例
            if (!transactionModalInstance) {
                transactionModalInstance = new bootstrap.Modal(document.getElementById('transactionModal'));
            }
            document.getElementById('transactionModalTitle').textContent = currentUserLang.includes('zh') ? `新增事务` : currentUserLang.includes('en') ? `Create Issue` : `新增事务` || `新增事务`;
            document.getElementById('transactionForm').reset();
            document.getElementById('transactionId').value = '';
            // new bootstrap.Modal(document.getElementById('transactionModal')).show();
            transactionModalInstance.show();
        });
    }

    // 保存事务按钮
    const saveForm = document.getElementById('transactionForm');
    if (saveForm) {
        saveForm.addEventListener('submit', function(e) {
            e.preventDefault();
            saveIssue();
        });
    }

    //重置按钮
    const searchFrom = document.getElementById('transactionFilterForm');
    if (searchFrom) {
        searchFrom.addEventListener('reset', function(e) {
            initIssueList({ page: 1, pageSize: 10 });;
        });
    }
}

/**
 * 绑定事务行操作事件
 */
// 替换 bindIssueActionEvents 函数中的查看按钮绑定
function bindIssueActionEvents() {
    // 委托到表格 body
    document.getElementById('transactionsTableBody').addEventListener('click', function(e) {
        const target = e.target.closest('.view-issue');
        if (target) {
            const id = target.getAttribute('data-id');
            viewIssue(id);
            return;
        }

        // 编辑按钮同理
        const editBtn = e.target.closest('.edit-issue');
        if (editBtn) {
            const id = editBtn.getAttribute('data-id');
            editIssue(id);
            return;
        }

        // 删除按钮同理
        const deleteBtn = e.target.closest('.delete-issue');
        if (deleteBtn) {
            const id = deleteBtn.getAttribute('data-id');
            if (confirm('确定要删除该事务吗？')) {
                deleteIssue(id);
            }
            return;
        }
    });
}

/**
 * 保存事务（新建或编辑）
 */
function saveIssue() {
    const id = document.getElementById('transactionId').value;
    const issue = {
        project: {
            projectIndex: document.getElementById('projectSelect').value
        },
        description: document.getElementById('descriptionInput').value,
        issueStatus: {
            dicIndex: document.getElementById('statusSelect').value
        },
        priority: {
            dicIndex: document.getElementById('prioritySelect').value
        },
        actionOwner: {
            userIndex: document.getElementById('executorSelect').value
        },
        issueOwner: {
            userIndex: document.getElementById('leaderSelect').value
        },
        issueReporter: {
            userIndex: window.currentUser
        },
        deadline: document.getElementById('expectedCompletionTimeInput').value
    };

    const url = id ? `/api/issues/${id}` : '/api/issues/create';
    const method = id ? 'PUT' : 'POST';

    fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(issue)
    })
        .then(response => {
            if (!response.ok) throw new Error('保存失败');
            return response.json();
        })
        .then(() => {
            alert('保存成功');
            bootstrap.Modal.getInstance(document.getElementById('transactionModal')).hide();
            // 刷新当前页列表
            const currentPage = document.querySelector('#pagination .page-item.active .page-link')?.getAttribute('data-page') || 1;
            initIssueList({ ...getCurrentFilters(), page: parseInt(currentPage), pageSize: 10 });
        })
        .catch(error => {
            alert('保存失败: ' + error.message);
        });
}

/**
 * 查看事务详情
 */
function viewIssue(id) {
    fetch(`/api/issues/${id}`)
        .then(response => {
            if (!response.ok) throw new Error('获取事务详情失败');
            return response.json();
        })
        .then(issue => {
            currentViewIssue = issue;
            console.log(issue.updateList);
            document.getElementById('thisactionId').value = issue.issueIndex;
            document.getElementById('viewProjectName').textContent = issue.projectName || '未设置';
            document.getElementById('viewDescription').textContent = currentUserLang.includes('zh') ? issue.description_cn : currentUserLang.includes('en') ? issue.description_en : issue.description || '无描述';
            document.getElementById('viewReporter').textContent = issue.issueReporterName || '未设置';
            document.getElementById('viewLeader').textContent = issue.issueOwnerName || '未设置';
            document.getElementById('viewExecutor').textContent = issue.actionOwnerName || '未设置';
            document.getElementById('viewExpectedCompletionTime').textContent = formatDate(issue.deadline);

            document.getElementById('chatWithReporter').setAttribute('data-user-id', issue.issueReporterQywx);
            document.getElementById('chatWithLeader').setAttribute('data-user-id', issue.issueOwnerQywx);
            document.getElementById('chatWithExecutor').setAttribute('data-user-id', issue.actionOwnerQywx);
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
                console.log(issue.updateList)
                const sortedUpdates = [...issue.updateList].sort((a, b) =>
                    new Date(b.updateTime).getTime() - new Date(a.updateTime).getTime()
                );

                let updatesHtml = '<div class="list-group">';
                sortedUpdates.forEach(update => {
                    updatesHtml += `
                        <div class="list-group-item">
                            <div class="d-flex w-100 justify-content-between">
                                <h6 class="mb-1">${update.updateOwner.lastname.concat("",update.updateOwner.firstname) || '未知用户'}</h6>
                                <small>${formatDate(update.updateTime)}</small>
                            </div>
                            <p class="mb-1">${currentUserLang.includes('zh') ? update.update_cn : currentUserLang.includes('en') ? update.update_en : update.statusUpdate || '无更新内容'}</p>
                        </div>
                    `;
                });
                updatesHtml += '</div>';
                updateListContainer.innerHTML = updatesHtml;
            } else {
                updateListContainer.innerHTML = '<p class="text-muted translate-item">暂无更新记录</p>';
            }

            // 显示查看详情模态框
            if (!window.viewTransactionModalInstance) {
                window.viewTransactionModalInstance = new bootstrap.Modal(document.getElementById('viewTransactionModal'));
            }
            window.viewTransactionModalInstance.show();

            // 绑定按钮点击事件
            bindWechatChatEvents();
        })
        .catch(error => {
            alert('获取详情失败: ' + error.message);
        });
}

/**
 * 编辑事务
 */
function editIssue(id) {
    fetch(`/api/issues/${id}`)
        .then(response => {
            if (!response.ok) throw new Error('获取事务数据失败');
            return response.json();
        })
        .then(issue => {

            document.getElementById('transactionModalTitle').textContent = currentUserLang.includes('zh') ? `编辑事务` : currentUserLang.includes('en') ? `Edit Issue` : `编辑事务` || `编辑事务`;
            document.getElementById('transactionId').value = issue.issueIndex;
            document.getElementById('projectSelect').value = issue.projectIndex;
            document.getElementById('descriptionInput').value = issue.description;
            document.getElementById('executorSelect').value = issue.actionOwnerIndex;
            document.getElementById('leaderSelect').value = issue.issueOwnerIndex;
            document.getElementById('expectedCompletionTimeInput').value = formatDate(issue.deadline);
            document.getElementById('statusSelect').value = issue.issueStatusIndex;

            // new bootstrap.Modal(document.getElementById('transactionModal')).show();
            if (!transactionModalInstance) {
                transactionModalInstance = new bootstrap.Modal(document.getElementById('transactionModal'));
            }
            transactionModalInstance.show();
        })
        .catch(error => {
            alert('获取数据失败: ' + error.message);
        });
}

/**
 * 删除事务
 */
function deleteIssue(id) {
    fetch(`/api/issues/${id}`, { method: 'DELETE' })
        .then(response => {
            if (!response.ok) throw new Error('删除失败');
            alert('删除成功');
            // 刷新当前页列表
            const currentPage = document.querySelector('#pagination .page-item.active .page-link')?.getAttribute('data-page') || 1;
            initIssueList({ ...getCurrentFilters(), page: parseInt(currentPage), pageSize: 10 });
        })
        .catch(error => {
            alert('删除失败: ' + error.message);
        });
}

/**
 * 更新分页控件
 * @param {number} total 总条数
 * @param {number} currentPage 当前页码（1基）
 * @param {number} pageSize 每页条数
 * @param {number} totalPages 总页数
 */
function updatePagination(total, currentPage, pageSize, totalPages) {
    const paginationContainer = document.getElementById('pagination');
    if (!paginationContainer) return;

    // 清空现有分页
    paginationContainer.innerHTML = '';

    // 总条数为0时不显示分页
    if (total === 0) {
        return;
    }

    // 上一页按钮
    const prevBtn = document.createElement('li');
    prevBtn.className = `page-item ${currentPage === 1 ? 'disabled' : ''}`;
    prevBtn.innerHTML = `<a class="page-link" href="#" data-page="${currentPage - 1}">上一页</a>`;
    paginationContainer.appendChild(prevBtn);

    // 页码按钮（简化版：只显示当前页、首页、末页和相邻页）
    for (let i = 1; i <= totalPages; i++) {
        // 只显示关键页码（避免过多页码）
        if (i === 1 || i === totalPages || Math.abs(i - currentPage) <= 1) {
            const pageBtn = document.createElement('li');
            pageBtn.className = `page-item ${i === currentPage ? 'active' : ''}`;
            pageBtn.innerHTML = `<a class="page-link" href="#" data-page="${i}">${i}</a>`;
            paginationContainer.appendChild(pageBtn);
        } else if (i === 2 && currentPage > 3) {
            // 省略号
            const ellipsis = document.createElement('li');
            ellipsis.className = 'page-item disabled';
            ellipsis.innerHTML = '<span class="page-link">...</span>';
            paginationContainer.appendChild(ellipsis);
        }
    }

    // 下一页按钮
    const nextBtn = document.createElement('li');
    nextBtn.className = `page-item ${currentPage === totalPages ? 'disabled' : ''}`;
    nextBtn.innerHTML = `<a class="page-link" href="#" data-page="${currentPage + 1}">下一页</a>`;
    paginationContainer.appendChild(nextBtn);

    // 绑定页码点击事件
    paginationContainer.querySelectorAll('.page-link').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const targetPage = parseInt(this.getAttribute('data-page'));
            if (isNaN(targetPage) || targetPage < 1 || targetPage > totalPages) return;

            // 判断当前场景：是否在执行全局搜索
            const keyword = document.getElementById('globalSearchInput').value.trim();
            if (keyword) {
                // 本地搜索场景：调用本地分页
                renderFilteredTable(keyword, targetPage, pageSize);
            } else {
                // 普通筛选场景：调用API分页
                const filters = {
                    projectIndex: document.getElementById('projectFilter').value,
                    issueStatus: document.getElementById('statusFilter').value,
                    actionOwner: document.getElementById('executorFilter').value,
                    startTime: document.getElementById('startDateFilter').value,
                    deadline: document.getElementById('endDateFilter').value,
                    page: targetPage,
                    pageSize: pageSize
                };
                initIssueList(filters);
            }

            // 更新分页控件（重新渲染当前页码状态）
            updatePagination(total, targetPage, pageSize, totalPages);
        });
    });
}

/**
 * 获取当前筛选条件
 */
function getCurrentFilters() {
    return {
        projectIndex: document.getElementById('projectFilter').value,
        issueStatus: document.getElementById('statusFilter').value,
        actionOwner: document.getElementById('executorFilter').value,
        startTime: document.getElementById('startDateFilter').value,
        deadline: document.getElementById('endDateFilter').value
    };
}

/**
 * 子页面专属：初始化添加更新相关事件（子页面加载后调用）
 */
function initIssueUpdateEvents() {
    // 1. 确保模态框实例只创建一次
    if (!addUpdateModal) {
        const modalElement = document.getElementById('addUpdateModal');
        if (!modalElement) {
            console.error('子页面未找到 addUpdateModal 模态框元素');
            return;
        }
        addUpdateModal = new bootstrap.Modal(modalElement);
    }

    // 2. 获取按钮元素（子页面 DOM 已加载，可直接获取）
    const openBtn = document.getElementById('openAddUpdateBtn');
    const saveBtn = document.getElementById('saveUpdateBtn');

    if (!openBtn || !saveBtn) {
        console.error('子页面未找到添加/保存更新按钮');
        return;
    }

    // 3. 先解绑原有事件（避免重复进入子页面导致多次绑定）
    openBtn.removeEventListener('click', handleOpenUpdateModal);
    saveBtn.removeEventListener('click', handleSaveUpdate);

    // 4. 重新绑定事件
    openBtn.addEventListener('click', handleOpenUpdateModal);
    saveBtn.addEventListener('click', handleSaveUpdate);
}

/**
 * 点击「添加事务更新」按钮的处理函数（独立抽离，方便解绑）
 */
function handleOpenUpdateModal() {
    try {
        if (!currentViewIssue) {
            alert('请先选择有效的事务');
            return;
        }

        // 填充表单默认值
        document.getElementById('currentIssueId').value = currentViewIssue.issueIndex;
        document.getElementById('updateExecutor').value = currentViewIssue.actionOwnerIndex || '';
        document.getElementById('updateLeader').value = currentViewIssue.issueOwnerIndex || '';
        document.getElementById('updateStatus').value = currentViewIssue.issueStatusIndex || '';
        document.getElementById('updateContent').value = '';

        // 打开模态框
        addUpdateModal.show();
    } catch (error) {
        console.error('打开更新模态框失败：', error);
        alert('打开失败：' + error.message, 'error');
    }
}

/**
 * 点击「保存更新」按钮的处理函数（独立抽离，方便解绑）
 */
function handleSaveUpdate() {
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
        actionOwnerIndex: document.getElementById('updateExecutor').value,
        issueOwnerIndex: document.getElementById('updateLeader').value
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
                addUpdateModal.hide();
                viewIssue(result.issue.issueIndex); // 刷新详情
            } else {
                alert('更新失败: ' + (result.message || '未知错误'), 'error');
            }
        })
        .catch(error => {
            alert('网络错误: ' + error.message, 'error');
        });
}

// 绑定企业微信聊天按钮事件
function bindWechatChatEvents() {
    // 解绑已有事件避免重复绑定
    document.getElementById('chatWithReporter').onclick = null;
    document.getElementById('chatWithLeader').onclick = null;
    document.getElementById('chatWithExecutor').onclick = null;

    // 绑定新事件
    document.getElementById('chatWithReporter').onclick = function() {
        openWechatChat(this.getAttribute('data-user-id'));
    };

    document.getElementById('chatWithLeader').onclick = function() {
        openWechatChat(this.getAttribute('data-user-id'));
    };

    document.getElementById('chatWithExecutor').onclick = function() {
        openWechatChat(this.getAttribute('data-user-id'));
    };
}

// 打开企业微信聊天窗口
async function openWechatChat(qywx) {
    if (!qywx) {
        alert('未获取到对应人员的企业微信信息');
        return;
    }

    // 桌面端企业微信本地协议唤醒（无需配置，直接跳转）
    try {
        // 步骤1：前端请求自己的后端
        const response = await fetch(`/api/common/getLaunchCode?userid=${qywx}`);
        const resData = await response.json();

        if (resData.code !== 200) {
            alert("查询企业微信用户失败：" + resData.msg);
            // 失败时降级：打开用户资料页（保留你原来的逻辑）
            const wxworkProfileUrl = `wxwork://opencontact?email=${encodeURIComponent(email)}`;
            window.location.href = wxworkProfileUrl;
            return;
        }

        // 步骤2：获取launch_code，使用正确的Scheme直接打开聊天窗口
        const { launch_code } = resData;
        let wxworkChatUrl = "";

        wxworkChatUrl = `wxwork://launch?launch_code=${encodeURIComponent(launch_code)}`;

        // 步骤3：唤起企业微信聊天窗口
        window.location.href = wxworkChatUrl;

    } catch (error) {
        console.error("企业微信聊天唤起失败：", error);
        // 异常时降级打开资料页
        const wxworkProfileUrl = `wxwork://opencontact?email=${encodeURIComponent(email)}`;
        window.location.href = wxworkProfileUrl;
    }
}