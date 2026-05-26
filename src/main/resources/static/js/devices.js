/**
 * 初始化设备管理模块
 */
function initDevices() {
    console.log('初始化设备管理模块');
    
    // 初始化设备列表
    initDeviceList();
    
    // 初始化搜索和筛选功能
    initDeviceFilters();
    
    // 初始化设备操作按钮事件
    initDeviceActionButtons();
}

/**
 * 初始化设备列表
 */
function initDeviceList() {
    const deviceTableBody = document.getElementById('deviceTableBody');
    if (!deviceTableBody) {
        console.warn('未找到设备表格容器');
        return;
    }
    
    // 模拟设备数据
    const devices = [
        { id: '1', name: '温度传感器 T201', type: '温度传感器', status: 'online', location: '车间A-1区', lastActive: '2023-07-15 09:45:22', firmware: 'v2.3.1' },
        { id: '2', name: '湿度传感器 H005', type: '湿度传感器', status: 'online', location: '车间A-2区', lastActive: '2023-07-15 09:30:15', firmware: 'v1.8.0' },
        { id: '3', name: '压力传感器 P102', type: '压力传感器', status: 'warning', location: '车间B-1区', lastActive: '2023-07-15 08:12:33', firmware: 'v3.1.2' },
        { id: '4', name: '流量传感器 F008', type: '流量传感器', status: 'offline', location: '车间B-2区', lastActive: '2023-07-14 22:30:45', firmware: 'v2.0.5' },
        { id: '5', name: '控制设备 C003', type: '控制设备', status: 'online', location: '控制室', lastActive: '2023-07-15 09:50:11', firmware: 'v4.2.0' },
        { id: '6', name: '网关设备 G001', type: '网关设备', status: 'online', location: '机房', lastActive: '2023-07-15 09:55:33', firmware: 'v5.0.3' }
    ];
    
    // 生成设备列表HTML
    let devicesHtml = '';
    devices.forEach(device => {
        let statusClass = '';
        let statusText = '';
        
        switch(device.status) {
            case 'online':
                statusClass = 'badge bg-success';
                statusText = '在线';
                break;
            case 'offline':
                statusClass = 'badge bg-danger';
                statusText = '离线';
                break;
            case 'warning':
                statusClass = 'badge bg-warning text-dark';
                statusText = '警告';
                break;
        }
        
        devicesHtml += `
            <tr>
                <td>${device.id}</td>
                <td>${device.name}</td>
                <td>${device.type}</td>
                <td><span class="${statusClass}">${statusText}</span></td>
                <td>${device.location}</td>
                <td>${device.lastActive}</td>
                <td>${device.firmware}</td>
                <td>
                    <div class="btn-group btn-group-sm">
                        <button class="btn btn-primary view-device" data-device-id="${device.id}">
                            <i class="fa fa-eye mr-1"></i>查看
                        </button>
                        <button class="btn btn-secondary edit-device" data-device-id="${device.id}">
                            <i class="fa fa-edit mr-1"></i>编辑
                        </button>
                        <button class="btn btn-danger delete-device" data-device-id="${device.id}">
                            <i class="fa fa-trash mr-1"></i>删除
                        </button>
                    </div>
                </td>
            </tr>
        `;
    });
    
    // 更新设备列表
    deviceTableBody.innerHTML = devicesHtml;
    
    // 为设备操作按钮添加事件
    document.querySelectorAll('.view-device').forEach(button => {
        button.addEventListener('click', function() {
            const deviceId = this.getAttribute('data-device-id');
            showDeviceDetail(deviceId);
        });
    });
    
    document.querySelectorAll('.edit-device').forEach(button => {
        button.addEventListener('click', function() {
            const deviceId = this.getAttribute('data-device-id');
            editDevice(deviceId);
        });
    });
    
    document.querySelectorAll('.delete-device').forEach(button => {
        button.addEventListener('click', function() {
            const deviceId = this.getAttribute('data-device-id');
            deleteDevice(deviceId);
        });
    });
}

/**
 * 初始化设备筛选功能
 */
function initDeviceFilters() {
    const searchInput = document.getElementById('deviceSearch');
    const statusFilter = document.getElementById('statusFilter');
    
    // 检查元素是否存在
    if (!searchInput || !statusFilter) {
        console.warn('未找到设备搜索或筛选元素');
        return;
    }
    
    // 搜索功能
    searchInput.addEventListener('input', filterDevices);
    
    // 状态筛选
    statusFilter.addEventListener('change', filterDevices);
}

/**
 * 筛选设备列表
 */
function filterDevices() {
    const searchInput = document.getElementById('deviceSearch');
    const statusFilter = document.getElementById('statusFilter');
    const deviceTableBody = document.getElementById('deviceTableBody');
    
    if (!searchInput || !statusFilter || !deviceTableBody) return;
    
    const searchTerm = searchInput.value.toLowerCase();
    const statusValue = statusFilter.value;
    
    // 获取所有设备行
    const deviceRows = deviceTableBody.querySelectorAll('tr');
    
    deviceRows.forEach(row => {
        // 获取行数据
        const textContent = row.textContent.toLowerCase();
        const statusBadge = row.querySelector('.badge');
        const statusText = statusBadge ? statusBadge.textContent.toLowerCase() : '';
        
        // 筛选逻辑
        const matchesSearch = textContent.includes(searchTerm);
        const matchesStatus = statusValue === 'all' || statusText.includes(statusValue);
        
        // 显示或隐藏行
        row.style.display = (matchesSearch && matchesStatus) ? '' : 'none';
    });
}

/**
 * 初始化设备操作按钮事件
 */
function initDeviceActionButtons() {
    const addDeviceBtn = document.getElementById('addDeviceBtn');
    if (addDeviceBtn) {
        addDeviceBtn.addEventListener('click', showAddDeviceForm);
    } else {
        console.warn('未找到添加设备按钮');
    }
    
    // 初始化设备详情模态框的关闭事件
    const modal = document.getElementById('deviceDetailModal');
    if (modal) {
        modal.addEventListener('hidden.bs.modal', function() {
            // 清除图表实例
            const chartDom = document.getElementById('deviceDataChart');
            if (chartDom && chartDom.__chartInstance) {
                chartDom.__chartInstance.dispose();
                chartDom.__chartInstance = null;
            }
        });
    }
}

/**
 * 显示设备详情
 * @param {string} deviceId - 设备ID
 */
function showDeviceDetail(deviceId) {
    // 检查模态框是否存在
    const modalElement = document.getElementById('deviceDetailModal');
    if (!modalElement) {
        app.showToast('未找到设备详情模态框');
        return;
    }
    
    // 模拟从API获取设备详情
    const device = {
        id: deviceId,
        name: `设备 ${deviceId}`,
        type: getDeviceType(deviceId),
        status: 'online',
        location: getDeviceLocation(deviceId),
        installationDate: '2023-01-' + (10 + parseInt(deviceId) % 20),
        lastMaintenance: '2023-06-' + (5 + parseInt(deviceId) % 25),
        firmwareVersion: 'v' + (2 + parseInt(deviceId) % 3) + '.' + (parseInt(deviceId) % 5) + '.' + (parseInt(deviceId) % 3),
        ipAddress: '192.168.1.' + (100 + parseInt(deviceId)),
        macAddress: '00:1B:44:' + Math.floor(Math.random() * 100).toString(16) + ':' + 
                    Math.floor(Math.random() * 256).toString(16) + ':' + 
                    Math.floor(Math.random() * 256).toString(16)
    };
    
    // 更新模态框内容
    document.getElementById('detailDeviceId').textContent = device.id;
    document.getElementById('detailDeviceName').textContent = device.name;
    document.getElementById('detailDeviceType').textContent = device.type;
    document.getElementById('detailDeviceStatus').textContent = device.status;
    document.getElementById('detailDeviceLocation').textContent = device.location;
    document.getElementById('detailInstallationDate').textContent = device.installationDate;
    document.getElementById('detailLastMaintenance').textContent = device.lastMaintenance;
    document.getElementById('detailFirmwareVersion').textContent = device.firmwareVersion;
    document.getElementById('detailIpAddress').textContent = device.ipAddress;
    document.getElementById('detailMacAddress').textContent = device.macAddress;
    
    // 显示模态框
    const modal = new bootstrap.Modal(modalElement);
    modal.show();
    
    // 初始化设备数据图表
    setTimeout(() => {
        initDeviceDataChart(deviceId);
    }, 100);
}

/**
 * 获取设备类型（根据ID模拟）
 */
function getDeviceType(deviceId) {
    const types = ['温度传感器', '湿度传感器', '压力传感器', '流量传感器', '控制设备', '网关设备'];
    return types[parseInt(deviceId) % types.length];
}

/**
 * 获取设备位置（根据ID模拟）
 */
function getDeviceLocation(deviceId) {
    const locations = ['车间A-1区', '车间A-2区', '车间B-1区', '车间B-2区', '控制室', '机房'];
    return locations[parseInt(deviceId) % locations.length];
}

/**
 * 初始化设备数据图表
 * @param {string} deviceId - 设备ID
 */
function initDeviceDataChart(deviceId) {
    const chartDom = document.getElementById('deviceDataChart');
    if (!chartDom) {
        console.warn('未找到设备数据图表容器');
        return;
    }
    
    // 如果已有图表实例，先销毁
    if (chartDom.__chartInstance) {
        chartDom.__chartInstance.dispose();
    }
    
    const myChart = echarts.init(chartDom);
    chartDom.__chartInstance = myChart;
    
    // 根据不同设备ID显示不同数据
    let seriesData = [];
    let yAxisName = '';
    let titleText = '';
    
    switch(parseInt(deviceId) % 3) {
        case 0:
            // 温度传感器数据
            seriesData = [65, 68, 70, 72, 75, 73, 71, 69, 67, 66, 68, 70];
            yAxisName = '温度 (°C)';
            titleText = '温度监测数据';
            break;
        case 1:
            // 湿度传感器数据
            seriesData = [45, 48, 50, 52, 55, 53, 51, 49, 47, 46, 48, 50];
            yAxisName = '湿度 (%)';
            titleText = '湿度监测数据';
            break;
        case 2:
            // 压力传感器数据
            seriesData = [1.2, 1.3, 1.4, 1.5, 1.4, 1.3, 1.2, 1.3, 1.4, 1.5, 1.6, 1.5];
            yAxisName = '压力 (MPa)';
            titleText = '压力监测数据';
            break;
    }
    
    // 生成时间轴
    const hours = [];
    const now = new Date();
    for (let i = 11; i >= 0; i--) {
        const hour = now.getHours() - i;
        hours.push(`${hour}:00`);
    }
    
    // 图表配置
    const option = {
        title: {
            text: titleText,
            left: 'center'
        },
        tooltip: {
            trigger: 'axis'
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
            data: hours
        },
        yAxis: {
            type: 'value',
            name: yAxisName
        },
        series: [
            {
                name: yAxisName,
                type: 'line',
                data: seriesData,
                smooth: true,
                lineStyle: {
                    width: 3
                },
                itemStyle: {
                    radius: 4
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
 * 编辑设备
 * @param {string} deviceId - 设备ID
 */
function editDevice(deviceId) {
    app.showToast(`编辑设备 #${deviceId}`);
    // 实际项目中会打开编辑设备的表单
}

/**
 * 删除设备
 * @param {string} deviceId - 设备ID
 */
function deleteDevice(deviceId) {
    if (confirm(`确定要删除设备 #${deviceId} 吗？`)) {
        app.showToast(`设备 #${deviceId} 已删除`);
        // 实际项目中会调用API删除设备并刷新列表
        initDeviceList();
    }
}

/**
 * 显示添加设备表单
 */
function showAddDeviceForm() {
    app.showToast('打开添加设备表单');
    // 实际项目中会打开添加设备的表单
}
    