/**
 * 初始化事件追踪模块
 */
function initEvents() {
    console.log('初始化事件追踪模块');

    // 初始化事件列表
    initEventList();

    // 初始化事件筛选功能
    initEventFilters();

    // 初始化事件详情模态框
    initEventDetailModal();
}

/**
 * 初始化事件列表
 */
function initEventList() {
    const eventTableBody = document.getElementById('eventTableBody');
    if (!eventTableBody) {
        console.warn('未找到事件表格容器');
        return;
    }

    // 模拟事件数据
    const events = [
        { id: 1, device: '温度传感器 #T201', eventType: '温度过高警告', severity: 'warning',
          timestamp: '2023-07-15 08:32:15', description: '设备温度超过阈值80°C，当前温度85°C' },
        { id: 2, device: '网关设备 #G003', eventType: '连接恢复', severity: 'info',
          timestamp: '2023-07-15 07:15:42', description: '设备连接已恢复正常' },
        { id: 3, device: '压力传感器 #P105', eventType: '离线', severity: 'critical',
          timestamp: '2023-07-14 23:48:33', description: '设备已超过30分钟未发送数据' },
        { id: 4, device: '湿度传感器 #H022', eventType: '校准完成', severity: 'info',
          timestamp: '2023-07-14 19:22:10', description: '设备校准已完成，精度±2%' },
        { id: 5, device: '控制设备 #C008', eventType: '固件更新', severity: 'info',
          timestamp: '2023-07-14 16:05:55', description: '设备固件已更新至版本v4.2.0' },
        { id: 6, device: '流量传感器 #F015', eventType: '流量异常', severity: 'warning',
          timestamp: '2023-07-14 14:30:22', description: '流量波动超过正常范围±15%' },
        { id: 7, device: '温度传感器 #T210', eventType: '传感器故障', severity: 'critical',
          timestamp: '2023-07-14 11:15:08', description: '传感器读数异常，可能存在硬件故障' },
        { id: 8, device: '网关设备 #G001', eventType: '网络拥堵', severity: 'warning',
          timestamp: '2023-07-14 09:45:33', description: '网络流量超过阈值，可能导致数据延迟' }
    ];

    // 生成事件列表HTML
    let eventsHtml = '';
    events.forEach(event => {
        let severityClass = '';
        let severityText = '';

        switch(event.severity) {
            case 'critical':
                severityClass = 'badge bg-danger';
                severityText = '严重';
                break;
            case 'warning':
                severityClass = 'badge bg-warning text-dark';
                severityText = '警告';
                break;
            case 'info':
                severityClass = 'badge bg-info';
                severityText = '信息';
                break;
        }

        eventsHtml += `
            <tr>
                <td>${event.id}</td>
                <td>${event.device}</td>
                <td>${event.eventType}</td>
                <td><span class="${severityClass}">${severityText}</span></td>
                <td>${event.timestamp}</td>
                <td>
                    <button class="btn btn-sm btn-primary view-event-detail" data-event-id="${event.id}">
                        详情
                    </button>
                </td>
            </tr>
        `;
    });

    // 更新事件列表
    eventTableBody.innerHTML = eventsHtml;

    // 绑定事件详情按钮
    document.querySelectorAll('.view-event-detail').forEach(button => {
        button.addEventListener('click', function() {
            const eventId = this.getAttribute('data-event-id');
            showEventDetail(eventId);
        });
    });
}

/**
 * 初始化事件筛选功能
 */
function initEventFilters() {
    const eventSearch = document.getElementById('eventSearch');
    const severityFilter = document.getElementById('severityFilter');
    const dateRangeFilter = document.getElementById('dateRangeFilter');

    // 检查元素是否存在
    if (!eventSearch || !severityFilter || !dateRangeFilter) {
        console.warn('未找到事件筛选元素');
        return;
    }

    // 绑定筛选事件
    eventSearch.addEventListener('input', filterEvents);
    severityFilter.addEventListener('change', filterEvents);
    dateRangeFilter.addEventListener('change', filterEvents);
}

/**
 * 筛选事件列表
 */
function filterEvents() {
    const eventSearch = document.getElementById('eventSearch');
    const severityFilter = document.getElementById('severityFilter');
    const dateRangeFilter = document.getElementById('dateRangeFilter');
    const eventTableBody = document.getElementById('eventTableBody');

    if (!eventSearch || !severityFilter || !dateRangeFilter || !eventTableBody) return;

    const searchTerm = eventSearch.value.toLowerCase();
    const severityValue = severityFilter.value;
    const dateRangeValue = dateRangeFilter.value;

    // 获取所有事件行
    const eventRows = eventTableBody.querySelectorAll('tr');

    eventRows.forEach(row => {
        // 获取行数据
        const textContent = row.textContent.toLowerCase();
        const severityBadge = row.querySelector('.badge');
        const severityText = severityBadge ? severityBadge.textContent.toLowerCase() : '';

        // 筛选逻辑
        const matchesSearch = textContent.includes(searchTerm);
        const matchesSeverity = severityValue === 'all' ||
                               (severityValue === 'critical' && severityText.includes('严重')) ||
                               (severityValue === 'warning' && severityText.includes('警告')) ||
                               (severityValue === 'info' && severityText.includes('信息'));

        // 简化的日期范围筛选（实际项目中需要更复杂的逻辑）
        const matchesDateRange = dateRangeValue === 'all' || true;

        // 显示或隐藏行
        row.style.display = (matchesSearch && matchesSeverity && matchesDateRange) ? '' : 'none';
    });
}

/**
 * 初始化事件详情模态框
 */
function initEventDetailModal() {
    const modal = document.getElementById('eventDetailModal');
    if (modal) {
        modal.addEventListener('hidden.bs.modal', function() {
            // 清除模态框内容
            document.getElementById('eventDetailContent').innerHTML = '';
        });
    } else {
        console.warn('未找到事件详情模态框');
    }
}

/**
 * 显示事件详情
 * @param {string} eventId - 事件ID
 */
function showEventDetail(eventId) {
    const modalElement = document.getElementById('eventDetailModal');
    if (!modalElement) {
        app.showToast('未找到事件详情模态框');
        return;
    }

    // 模拟从API获取事件详情
    const event = getEventById(eventId);
    if (!event) {
        app.showToast('未找到事件详情');
        return;
    }

    let severityClass = '';
    switch(event.severity) {
        case 'critical':
            severityClass = 'text-bg-danger';
            break;
        case 'warning':
            severityClass = 'text-bg-warning';
            break;
        case 'info':
            severityClass = 'text-bg-info';
            break;
    }

    // 生成事件详情HTML
    const detailHtml = `
        <div class="mb-3">
            <h5>事件 #${event.id} 详情</h5>
            <hr>
        </div>
        <div class="row mb-3">
            <div class="col-md-6">
                <strong>设备:</strong> ${event.device}
            </div>
            <div class="col-md-6">
                <strong>事件类型:</strong> ${event.eventType}
            </div>
        </div>
        <div class="row mb-3">
            <div class="col-md-6">
                <strong>严重程度:</strong> <span class="badge ${severityClass}">${
                    event.severity === 'critical' ? '严重' : 
                    event.severity === 'warning' ? '警告' : '信息'
                }</span>
            </div>
            <div class="col-md-6">
                <strong>发生时间:</strong> ${event.timestamp}
            </div>
        </div>
        <div class="mb-3">
            <strong>事件描述:</strong>
            <p class="mt-1">${event.description}</p>
        </div>
        <div class="mb-3">
            <strong>可能的原因:</strong>
            <ul class="mt-1">
                <li>${getPossibleCauses(event.eventType)}</li>
            </ul>
        </div>
        <div class="mb-3">
            <strong>建议措施:</strong>
            <ul class="mt-1">
                <li>${getRecommendedActions(event.eventType)}</li>
            </ul>
        </div>
        <div class="mb-3">
            <strong>处理状态:</strong>
            <select class="form-select mt-1" id="eventStatusSelect">
                <option value="unhandled">未处理</option>
                <option value="processing">处理中</option>
                <option value="resolved">已解决</option>
            </select>
        </div>
        <div class="mb-3">
            <strong>处理记录:</strong>
            <textarea class="form-control mt-1" rows="3" placeholder="输入处理记录..."></textarea>
        </div>
    `;

    // 更新模态框内容
    const detailContent = document.getElementById('eventDetailContent');
    if (detailContent) {
        detailContent.innerHTML = detailHtml;
    }

    // 显示模态框
    const modal = new bootstrap.Modal(modalElement);
    modal.show();
}

/**
 * 根据ID获取事件（模拟）
 */
function getEventById(eventId) {
    // 模拟事件数据库
    const events = [
        { id: 1, device: '温度传感器 #T201', eventType: '温度过高警告', severity: 'warning',
          timestamp: '2023-07-15 08:32:15', description: '设备温度超过阈值80°C，当前温度85°C' },
        { id: 2, device: '网关设备 #G003', eventType: '连接恢复', severity: 'info',
          timestamp: '2023-07-15 07:15:42', description: '设备连接已恢复正常' },
        { id: 3, device: '压力传感器 #P105', eventType: '离线', severity: 'critical',
          timestamp: '2023-07-14 23:48:33', description: '设备已超过30分钟未发送数据' },
        { id: 4, device: '湿度传感器 #H022', eventType: '校准完成', severity: 'info',
          timestamp: '2023-07-14 19:22:10', description: '设备校准已完成，精度±2%' },
        { id: 5, device: '控制设备 #C008', eventType: '固件更新', severity: 'info',
          timestamp: '2023-07-14 16:05:55', description: '设备固件已更新至版本v4.2.0' },
        { id: 6, device: '流量传感器 #F015', eventType: '流量异常', severity: 'warning',
          timestamp: '2023-07-14 14:30:22', description: '流量波动超过正常范围±15%' },
        { id: 7, device: '温度传感器 #T210', eventType: '传感器故障', severity: 'critical',
          timestamp: '2023-07-14 11:15:08', description: '传感器读数异常，可能存在硬件故障' },
        { id: 8, device: '网关设备 #G001', eventType: '网络拥堵', severity: 'warning',
          timestamp: '2023-07-14 09:45:33', description: '网络流量超过阈值，可能导致数据延迟' }
    ];

    return events.find(event => event.id.toString() === eventId.toString());
}

/**
 * 获取可能的原因（模拟）
 */
function getPossibleCauses(eventType) {
    const causes = {
        '温度过高警告': '环境温度升高、设备散热不良或传感器故障',
        '连接恢复': '网络问题已解决或设备已重新启动',
        '离线': '网络中断、电源故障或设备硬件问题',
        '校准完成': '定期维护或手动触发的校准程序已完成',
        '固件更新': '系统自动更新或手动触发的固件升级',
        '流量异常': '管道压力变化、阀门状态改变或传感器故障',
        '传感器故障': '传感器老化、接线问题或内部元件损坏',
        '网络拥堵': '数据传输量过大、网络设备故障或带宽不足'
    };

    return causes[eventType] || '原因未明确';
}

/**
 * 获取建议措施（模拟）
 */
function getRecommendedActions(eventType) {
    const actions = {
        '温度过高警告': '检查设备散热情况，确认环境温度是否正常，必要时校准传感器',
        '连接恢复': '确认设备数据是否正常同步，检查历史数据是否完整',
        '离线': '检查设备电源和网络连接，必要时进行现场检查',
        '校准完成': '验证校准结果，记录校准数据用于后续分析',
        '固件更新': '确认设备功能是否正常，检查是否有新功能需要配置',
        '流量异常': '检查管道系统是否正常，确认是否有泄漏或堵塞情况',
        '传感器故障': '替换故障传感器，检查相关设备是否受影响',
        '网络拥堵': '优化数据传输策略，考虑增加带宽或升级网络设备'
    };

    return actions[eventType] || '请联系技术支持获取帮助';
}
