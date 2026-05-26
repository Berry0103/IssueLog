let userModalInstance = null;

/**
 * 初始化用户管理模块
 */
function initUserManagement() {
    console.log('初始化用户管理模块');
    // 加载用户列表
    initUserList();

    // 绑定事件
    initUserActionButtons();

    loadDepts();
}

function loadDepts(){
    const deptSelect = document.getElementById('deptSelect');

    if (!deptSelect) return;

    fetch('/api/dept/deptlist')
        .then(response => response.json())
        .then(depts => {
            // 按用户姓名排序
            const sortedDepts = [...depts].sort((a, b) => {
                return a.deptName.localeCompare(b.deptName, 'zh-CN');
            });

            // 清空现有选项（保留第一个"全部"或"请选择"选项）
            while (deptSelect.options.length > 1) {
                deptSelect.remove(0);
            }

            // 用于存储原始选项，供搜索功能使用
            const deptOptionsData = [];

            sortedDepts.forEach(dept => {
                const optionText = dept.deptName + "-" + dept.description;
                const optionValue = dept.deptIndex;

                // 存储选项数据
                deptOptionsData.push({ text: optionText, value: optionValue });

                // 为模态框添加选项
                const selectOption = document.createElement('option');
                selectOption.value = optionValue;
                selectOption.textContent = optionText;
                deptSelect.appendChild(selectOption);
            });
        })
        .catch(error => console.error('加载部门列表失败:', error));
}

/**
 * 初始化用户列表
 */
function initUserList() {
    const userTableBody = document.getElementById('userTableBody');
    if (!userTableBody) {
        console.warn('未找到用户表格容器');
        return;
    }

    fetch(`/api/user/allusers`)
        .then(response => {
            if (!response.ok) throw new Error('获取用户列表失败');
            return response.json();
        })
        .then(users => {
            const sortedUsers = [...users].sort((a, b) => {
                return a.fullName.localeCompare(b.fullName, 'zh-CN');
            });

            // 生成用户列表HTML
            let usersHtml = '';

            sortedUsers.forEach(user => {
                const statusMap = {
                    'ON DUTY': { class: 'bg-success', text: '在岗' },
                    'ON SITE': { class: 'column-progress', text: '出差' },
                    'BREAK DAY': { class: 'column-pending', text: '休假' },
                    'LEAVE': { class: 'bg-danger', text: '离职' }
                };
                const status = statusMap[user.userStateName] || { class: 'column-open', text: user.userStateName };

                usersHtml += `
                    <tr>
                        <td class="d-none">${user.userIndex}</td>
                        <td class="text-center">${user.userName}</td>
                        <td>${user.fullName}</td>
                        <td>${user.email}</td>
                        <td class="text-center">${user.departmentName}</td>
                        <td class="text-center">${user.rolerName}</td>
                        <td class="text-center"><span class="badge ${status.class}">${status.text}</span></td>
                        <td class="text-center">${formatDate(user.lastLogin)}</td>
                        <td class="text-center">
                            <div class="btn-group btn-group-sm">
                                <button class="btn btn-secondary edit-user" data-user-id="${user.userIndex}">
                                    <i class="fa fa-edit mr-1"></i>编辑
                                </button>
                            </div>
                        </td>
                    </tr>
                `;
            });

            // 更新用户列表
            userTableBody.innerHTML = usersHtml;

            document.querySelectorAll('.edit-user').forEach(button => {
                button.addEventListener('click', function() {
                    const userId = this.getAttribute('data-user-id');
                    editUser(userId);
                });
            });
        })
        .catch(error => {
            console.error('加载用户列表失败:', error);
            userTableBody.innerHTML = '<tr><td colspan="9" class="text-center text-danger">加载失败，请重试</td></tr>';
        });
}

/**
 * 初始化用户操作按钮事件
 */
function initUserActionButtons() {
    const addUserBtn = document.getElementById('addUserBtn');
    if (addUserBtn) {
        addUserBtn.addEventListener('click', () => {
            // 复用同一个实例
            if (!userModalInstance) {
                userModalInstance = new bootstrap.Modal(document.getElementById('userModal'));
            }
            document.getElementById('userModalTitle').textContent = currentUserLang.includes('zh') ? `新增用户` : currentUserLang.includes('en') ? `Create User` : `新增用户` || `新增用户`;
            document.getElementById('userForm').reset();
            document.getElementById('userId').value = '';
            // new bootstrap.Modal(document.getElementById('transactionModal')).show();
            userModalInstance.show();
        });
    } else {
        console.warn('未找到添加用户按钮');
    }

    // 保存按钮
    const saveForm = document.getElementById('userForm');
    if (saveForm) {
        saveForm.addEventListener('submit', function(e) {
            e.preventDefault();
            saveUser();
        });
    }
}

/**
 * 编辑用户
 * @param {string} userId - 用户ID
 */
function editUser(userId) {
    fetch(`/api/user/${userId}`)
        .then(response => {
            if (!response.ok) throw new Error('获取事务数据失败');
            return response.json();
        })
        .then(user => {
            document.getElementById('userModalTitle').textContent = currentUserLang.includes('zh') ? `编辑用户` : currentUserLang.includes('en') ? `Edit User` : `编辑用户` || `编辑用户`;
            document.getElementById('userId').value = user.userIndex;
            document.getElementById('username').value = user.userName;
            document.getElementById('lastname').value = user.lastname;
            document.getElementById('firstname').value = user.firstname;
            document.getElementById('email').value = user.email;
            document.getElementById('roleLevel').value = user.rolerIndex;
            document.getElementById('status').value = user.userStateIndex;
            document.getElementById('deptSelect').value = user.departmentIndex;

            // new bootstrap.Modal(document.getElementById('transactionModal')).show();
            if (!userModalInstance) {
                userModalInstance = new bootstrap.Modal(document.getElementById('userModal'));
            }
            userModalInstance.show();
        })
        .catch(error => {
            alert('获取数据失败: ' + error.message);
        });
}

/**
 * 保存用户
 */
function saveUser() {
    const id = document.getElementById('userId').value;
    const user = {
        username: document.getElementById('username').value,
        password: '',
        department: {
            deptIndex: document.getElementById('deptSelect').value
        },
        email: document.getElementById('email').value,
        firstname: document.getElementById('firstname').value,
        lastname: document.getElementById('lastname').value,
        roler: {
            rolerIndex: document.getElementById('roleLevel').value
        },
        userState: {
            dicIndex: document.getElementById('status').value
        }
    };

    const url = id ? `/api/user/${id}` : '/api/user/create';
    const method = id ? 'PUT' : 'POST';

    fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(user)
    })
        .then(response => {
            if (!response.ok) throw new Error('保存失败');
            return response.json();
        })
        .then(() => {
            alert('保存成功');
            bootstrap.Modal.getInstance(document.getElementById('userModal')).hide();
            // 刷新当前页列表
            initUserList();
        })
        .catch(error => {
            alert('保存失败: ' + error.message);
        });
}