let projectModalInstance = null;

/**
 * 初始化用户管理模块
 */
function initProjectManagement() {
    console.log('初始化项目管理模块');
    // 加载用户列表
    initProjectList();

    // 绑定事件
    initProjectActionButtons();

    loadSelectors();
}

function loadSelectors(){
    const typeSelect = document.getElementById('typeSelect');
    const powerSelect = document.getElementById('powerSelect');
    const customerSelect = document.getElementById('customerSelect');

    if (!typeSelect || !powerSelect) return;

    const typeIndex = "ee0cda93-49c2-404d-a8b3-37630b679b93";
    const powerIndex = "2c872c8f-99fe-4398-bd1f-3a62f11d8fcf";
    fetch(`/api/common/dicts/${typeIndex}`)
        .then(response => response.json())
        .then(types => {
            // 按用户姓名排序
            const sortedTypes = [...types].sort((a, b) => {
                return a.dicName.localeCompare(b.dicName, 'zh-CN');
            });

            // 清空现有选项（保留第一个"全部"或"请选择"选项）
            while (typeSelect.options.length > 1) {
                typeSelect.remove(1);
            }

            // 用于存储原始选项，供搜索功能使用
            const typeOptionsData = [];

            sortedTypes.forEach(type => {
                const optionText = type.dicName + "-" + type.description;
                const optionValue = type.dicIndex;

                // 存储选项数据
                typeOptionsData.push({ text: optionText, value: optionValue });

                // 为模态框添加选项
                const selectOption = document.createElement('option');
                selectOption.value = optionValue;
                selectOption.textContent = optionText;
                typeSelect.appendChild(selectOption);
            });
        })
        .catch(error => console.error('加载类型失败:', error));
    fetch(`/api/common/dicts/${powerIndex}`)
        .then(response => response.json())
        .then(powers => {
            // 按用户姓名排序
            const sortedPowers = [...powers].sort((a, b) => {
                return a.dicName.localeCompare(b.dicName, 'zh-CN');
            });

            // 清空现有选项（保留第一个"全部"或"请选择"选项）
            while (powerSelect.options.length > 1) {
                powerSelect.remove(1);
            }

            // 用于存储原始选项，供搜索功能使用
            const powerOptionsData = [];

            sortedPowers.forEach(power => {
                const optionText1 = power.dicName + "-" + power.description;
                const optionValue1 = power.dicIndex;

                // 存储选项数据
                powerOptionsData.push({ text: optionText1, value: optionValue1 });

                // 为模态框添加选项
                const selectOption1 = document.createElement('option');
                selectOption1.value = optionValue1;
                selectOption1.textContent = optionText1;
                powerSelect.appendChild(selectOption1);
            });
        })
        .catch(error => console.error('加载类型失败:', error));

    fetch('/api/customers')
        .then(response => response.json())
        .then(customers => {
            // 按用户姓名排序
            const sortedCustomers = [...customers].sort((a, b) => {
                return a.customerCode.localeCompare(b.customerCode, 'zh-CN');
            });

            // 清空现有选项（保留第一个"全部"或"请选择"选项）
            while (customerSelect.options.length > 1) {
                customerSelect.remove(1);
            }

            // 用于存储原始选项，供搜索功能使用
            const customerOptionsData = [];

            sortedCustomers.forEach(customer => {
                const optionText2 = customer.customerCode + "-" + customer.customerName;
                const optionValue2 = customer.customerIndex;

                // 存储选项数据
                customerOptionsData.push({ text: optionText2, value: optionValue2 });

                // 为模态框添加选项
                const selectOption2 = document.createElement('option');
                selectOption2.value = optionValue2;
                selectOption2.textContent = optionText2;
                customerSelect.appendChild(selectOption2);
            });
        })
        .catch(error => console.error('加载客户失败:', error));
}

/**
 * 初始化用户列表
 */
function initProjectList() {
    const projectTableBody = document.getElementById('projectTableBody');
    if (!projectTableBody) {
        console.warn('未找到项目表格容器');
        return;
    }

    fetch(`/api/project/projects`)
        .then(response => {
            if (!response.ok) throw new Error('获取项目列表失败');
            return response.json();
        })
        .then(projects => {
            const sortedProjects = [...projects].sort((a, b) => {
                return a.projectName.localeCompare(b.projectName, 'zh-CN');
            });

            // 生成用户列表HTML
            let projectsHtml = '';

            sortedProjects.forEach(project => {
                projectsHtml += `
                    <tr>
                        <td class="d-none">${project.projectIndex}</td>
                        <td class="text-center">${project.projectName}</td>
                        <td class="text-center">${project.typeName}</td>
                        <td class="text-center">${project.powerName}</td>
                        <td class="text-center">${project.customerName}</td>
                        <td class="text-center">${project.description}</td>
                        <td class="text-center">
                            <div class="btn-group btn-group-sm">
                                <button class="btn btn-secondary edit-project" data-project-id="${project.projectIndex}">
                                    <i class="fa fa-edit mr-1"></i>编辑
                                </button>
                            </div>
                        </td>
                    </tr>
                `;
            });

            // 更新用户列表
            projectTableBody.innerHTML = projectsHtml;

            document.querySelectorAll('.edit-project').forEach(button => {
                button.addEventListener('click', function() {
                    const projectId = this.getAttribute('data-project-id');
                    editProject(projectId);
                });
            });
        })
        .catch(error => {
            console.error('加载项目列表失败:', error);
            projectTableBody.innerHTML = '<tr><td colspan="9" class="text-center text-danger">加载失败，请重试</td></tr>';
        });
}

/**
 * 初始化项目操作按钮事件
 */
function initProjectActionButtons() {
    const addProjectBtn = document.getElementById('addProjectBtn');
    if (addProjectBtn) {
        addProjectBtn.addEventListener('click', () => {
            // 复用同一个实例
            if (!projectModalInstance) {
                projectModalInstance = new bootstrap.Modal(document.getElementById('projectModal'));
            }
            document.getElementById('projectModalTitle').textContent = currentUserLang.includes('zh') ? `新增项目` : currentUserLang.includes('en') ? `Create Project` : `新增项目` || `新增项目`;
            document.getElementById('projectForm').reset();
            document.getElementById('projectId').value = '';
            // new bootstrap.Modal(document.getElementById('transactionModal')).show();
            projectModalInstance.show();
        });
    } else {
        console.warn('未找到添加项目按钮');
    }

    // 保存按钮
    const saveForm = document.getElementById('projectForm');
    if (saveForm) {
        saveForm.addEventListener('submit', function(e) {
            e.preventDefault();
            saveProject();
        });
    }
}

/**
 * 编辑项目
 * @param {string} projectId - 项目ID
 */
function editProject(projectId) {
    fetch(`/api/project/${projectId}`)
        .then(response => {
            if (!response.ok) throw new Error('获取事务数据失败');
            return response.json();
        })
        .then(project => {
            document.getElementById('projectModalTitle').textContent = currentUserLang.includes('zh') ? `编辑用户` : currentUserLang.includes('en') ? `Edit Project` : `编辑用户` || `编辑用户`;
            document.getElementById('projectId').value = project.projectIndex;
            document.getElementById('projectname').value = project.projectName;
            document.getElementById('typeSelect').value = project.typeIndex;
            document.getElementById('powerSelect').value = project.powerIndex;
            document.getElementById('customerSelect').value = project.customerIndex;
            document.getElementById('proDescirption').value = project.description;

            // new bootstrap.Modal(document.getElementById('transactionModal')).show();
            if (!projectModalInstance) {
                projectModalInstance = new bootstrap.Modal(document.getElementById('projectModal'));
            }
            projectModalInstance.show();
        })
        .catch(error => {
            alert('获取数据失败: ' + error.message);
        });
}

/**
 * 保存用户
 */
function saveProject() {
    const id = document.getElementById('projectId').value;
    const project = {
        projectName: document.getElementById('projectname').value,
        projectType: {
            dicIndex: document.getElementById('typeSelect').value
        },
        power: {
            dicIndex: document.getElementById('powerSelect').value
        },
        customer: {
            customerIndex: document.getElementById('customerSelect').value
        },
        description: document.getElementById('proDescirption').value
    };

    const url = id ? `/api/project/${id}` : '/api/project/create';
    const method = id ? 'PUT' : 'POST';

    fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(project)
    })
        .then(response => {
            if (!response.ok) throw new Error('保存失败');
            return response.json();
        })
        .then(() => {
            alert('保存成功');
            bootstrap.Modal.getInstance(document.getElementById('projectModal')).hide();
            // 刷新当前页列表
            initProjectList();
        })
        .catch(error => {
            alert('保存失败: ' + error.message);
        });
}