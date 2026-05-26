/**
 * 初始化客户管理模块
 */
function initCustomerManagement() {
    console.log('初始化客户管理模块');

    // 初始化客户列表
    initCustomerList();

    // 初始化操作按钮事件
    initCustomerActionButtons();

    // 初始化搜索功能
    initCustomerSearch();
}

/**
 * 初始化客户列表
 */
function initCustomerList() {
    const tableBody = document.getElementById('customerTableBody');
    if (!tableBody) return;

    // 调用API获取客户列表
    fetch('/api/customers')
        .then(response => response.json())
        .then(customers => {
            let html = '';
            customers.forEach(customer => {
                const statusClass = customer.status === 1 ? 'bg-success' : 'bg-secondary';
                const statusText = customer.status === 1 ? '正常' : '禁用';

                html += `
                    <tr>
                        <td>${customer.customerCode}</td>
                        <td>${customer.customerName}</td>
                        <td>${getAreaName(customer.areaCode)}</td>
                        <td>${formatLevel(customer.customerLevel)}</td>
                        <td><span class="badge ${statusClass}">${statusText}</span></td>
                        <td>
                            <div class="btn-group btn-group-sm">
                                <button class="btn btn-primary view-customer" data-id="${customer.id}">查看</button>
                                <button class="btn btn-secondary edit-customer" data-id="${customer.id}">编辑</button>
                                <button class="btn btn-danger delete-customer" data-id="${customer.id}">删除</button>
                                <button class="btn btn-info bind-user" data-id="${customer.id}">关联用户</button>
                            </div>
                        </td>
                    </tr>
                `;
            });
            tableBody.innerHTML = html;

            // 绑定按钮事件
            bindCustomerActionEvents();
        })
        .catch(error => console.error('加载客户列表失败:', error));
}

/**
 * 初始化操作按钮事件
 */
function initCustomerActionButtons() {
    const addBtn = document.getElementById('addCustomerBtn');
    if (addBtn) {
        addBtn.addEventListener('click', () => {
            document.getElementById('customerModalTitle').textContent = '添加客户';
            document.getElementById('customerForm').reset();
            document.getElementById('customerId').value = '';
            new bootstrap.Modal(document.getElementById('customerModal')).show();
        });
    }

    // 保存客户按钮事件
    document.getElementById('saveCustomerBtn').addEventListener('click', saveCustomer);
}

/**
 * 绑定客户操作事件
 */
function bindCustomerActionEvents() {
    // 编辑客户
    document.querySelectorAll('.edit-customer').forEach(btn => {
        btn.addEventListener('click', function() {
            const id = this.getAttribute('data-id');
            loadCustomerForEdit(id);
        });
    });

    // 删除客户
    document.querySelectorAll('.delete-customer').forEach(btn => {
        btn.addEventListener('click', function() {
            const id = this.getAttribute('data-id');
            if (confirm('确定要删除该客户吗？')) {
                deleteCustomer(id);
            }
        });
    });
}

/**
 * 保存客户信息
 */
function saveCustomer() {
    const id = document.getElementById('customerId').value;
    const customer = {
        customerCode: document.getElementById('customerCode').value,
        customerName: document.getElementById('customerName').value,
        areaCode: document.getElementById('areaCode').value,
        customerLevel: document.getElementById('customerLevel').value,
        status: parseInt(document.getElementById('customerStatus').value)
    };

    const url = id ? `/api/customers/${id}` : '/api/customers';
    const method = id ? 'PUT' : 'POST';

    fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(customer)
    })
        .then(response => {
            if (!response.ok) throw new Error('保存失败');
            return response.json();
        })
        .then(() => {
            app.showToast('保存成功');
            bootstrap.Modal.getInstance(document.getElementById('customerModal')).hide();
            initCustomerList(); // 刷新列表
        })
        .catch(error => {
            app.showToast('保存失败: ' + error.message);
        });
}

/**
 * 删除客户
 */
function deleteCustomer(id) {
    fetch(`/api/customers/${id}`, { method: 'DELETE' })
        .then(response => {
            if (!response.ok) throw new Error('删除失败');
            app.showToast('删除成功');
            initCustomerList(); // 刷新列表
        })
        .catch(error => {
            app.showToast('删除失败: ' + error.message);
        });
}

/**
 * 初始化搜索功能
 */
function initCustomerSearch() {
    const searchInput = document.getElementById('customerSearch');
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            const keyword = this.value.trim();
            if (keyword.length > 0) {
                fetch(`/api/customers/search?keyword=${encodeURIComponent(keyword)}`)
                    .then(response => response.json())
                    .then(customers => {
                        // 渲染搜索结果（逻辑同initCustomerList）
                    })
                    .catch(error => console.error('搜索客户失败:', error));
            } else {
                initCustomerList(); // 空搜索时显示全部
            }
        });
    }
}

/**
 * 格式化客户等级显示
 */
function formatLevel(level) {
    const levels = {
        'VIP': 'VIP客户',
        'NORMAL': '普通客户',
        'TRIAL': '试用客户'
    };
    return levels[level] || level;
}

/**
 * 获取区域名称（从字典表获取）
 */
function getAreaName(areaCode) {
    // 实际项目中应从字典接口获取
    const areaMap = {
        'EAST': '华东区',
        'WEST': '西区',
        'NORTH': '北区',
        'SOUTH': '南区'
    };
    return areaMap[areaCode] || areaCode || '未知区域';
}