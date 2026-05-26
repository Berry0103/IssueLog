/**
 * 主应用控制器
 */
class AppController {
    constructor() {
        // 当前活跃的模块
        this.activeModule = null;
        
        // 页面元素 - 增加存在性检查
        this.contentContainer = document.getElementById('contentContainer');
        if (!this.contentContainer) {
            console.error('错误: 未找到contentContainer元素，请检查HTML结构');
            // 创建一个临时容器避免后续错误
            this.contentContainer = document.createElement('div');
            document.body.appendChild(this.contentContainer);
        }
        
        this.navLinks = document.querySelectorAll('.nav-link');
        this.loadingIndicator = document.getElementById('loadingIndicator');
        this.pageTitleElement = document.getElementById('pageTitle');
        this.sidebar = document.querySelector('.sidebar');
        this.mobileMenuToggle = document.querySelector('.mobile-menu-toggle');
        this.logoimage = document.getElementById('leftLogo');
        
        // 初始化应用
        this.init();
    }
    
    /**
     * 初始化应用
     */
    init() {
        // 创建侧边栏切换按钮
        this.createSidebarToggle();
        
        // 绑定导航事件
        this.bindNavEvents();
        
        // 绑定用户菜单事件
        this.bindUserMenuEvents();
        
        // 绑定移动端菜单事件
        this.bindMobileMenuEvents();
        
        // 绑定侧边栏切换事件
        this.bindSidebarToggleEvent();
        
        // 加载默认模块
        // this.loadModule(this.activeModule);
        
        // 初始化通知提示框
        this.initToast();
        
        // 初始化响应式布局
        this.initResponsiveLayout();
    }
    
    /**
     * 创建侧边栏切换按钮
     */
    createSidebarToggle() {
        if (!this.sidebar) return;
        
        // // 检查是否已存在切换按钮
        // if (!document.querySelector('.sidebar-toggle')) {
        //     const toggleBtn = document.createElement('button');
        //     toggleBtn.className = 'sidebar-toggle';
        //     toggleBtn.innerHTML = '<i class="fa fa-angle-double-right"></i>';
        //     toggleBtn.title = '收起/展开侧边栏';
        //     this.sidebar.appendChild(toggleBtn);
        // }
    }
    
    /**
     * 绑定侧边栏切换事件
     */
    bindSidebarToggleEvent() {
        const toggleBtn = document.querySelector('.sidebar-toggle');
        if (toggleBtn && this.sidebar) {
            toggleBtn.addEventListener('click', () => {
                this.toggleSidebar();
            });
        }
    }
    
    /**
     * 切换侧边栏状态
     */
    toggleSidebar() {
        if (!this.sidebar) return;
        
        // 切换收起/展开状态
        this.sidebar.classList.toggle('collapsed');
        
        // 更新切换按钮图标
        const toggleBtn = document.querySelector('.sidebar-toggle');
        if (toggleBtn) {
            const icon = toggleBtn.querySelector('i');
            if (this.sidebar.classList.contains('collapsed')) {
                icon.className = 'fa fa-angle-double-left';
            } else {
                icon.className = 'fa fa-angle-double-right';
            }
        }
    }
    
    /**
     * 绑定移动端菜单事件
     */
    bindMobileMenuEvents() {
        if (this.mobileMenuToggle && this.sidebar) {
            this.mobileMenuToggle.addEventListener('click', () => {
                this.sidebar.classList.toggle('open');
                this.sidebar.classList.toggle('collapsed',false);
            });
            
            // 在移动设备上点击导航链接后关闭侧边栏
            this.navLinks.forEach(link => {
                link.addEventListener('click', () => {
                    if (window.innerWidth <= 768) {
                        this.sidebar.classList.remove('open');
                        this.sidebar.classList.toggle('collapsed',true);
                    }
                });
            });
        }
    }
    
    /**
     * 初始化响应式布局
     */
    initResponsiveLayout() {
        // 根据屏幕宽度设置初始状态
        this.updateLayoutForScreenSize();
        
        // 监听窗口大小变化
        window.addEventListener('resize', () => {
            this.updateLayoutForScreenSize();
        });
    }
    
    /**
     * 根据屏幕尺寸更新布局
     */
    updateLayoutForScreenSize() {
        if (!this.sidebar) return;
        
        // 在大屏幕上
        if (window.innerWidth > 768) {
            this.sidebar.classList.remove('open'); // 移除移动端打开状态
            // 可以根据需要设置默认是否收起
            this.sidebar.classList.remove('collapsed');
            this.logoimage.classList.remove('logo-image-mobile');
            this.logoimage.classList.add('logo-image');
        } else {
            // 在小屏幕上默认隐藏侧边栏
            this.sidebar.classList.add('collapsed');
            this.logoimage.classList.remove('logo-image');
            this.logoimage.classList.add('logo-image-mobile');
        }
    }
    
    /**
     * 绑定导航事件
     */
    bindNavEvents() {
        if (this.navLinks.length === 0) {
            console.warn('未找到导航链接元素');
            return;
        }
        
        this.navLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                
                // 获取模块名称
                const module = link.getAttribute('data-module');
                if (!module || module === this.activeModule) return;
                
                // 加载模块
                this.loadModule(module);
            });
        });
    }
    
    /**
     * 绑定用户菜单事件
     */
    bindUserMenuEvents() {
        const userMenuBtn = document.getElementById('userMenuBtn');
        const userMenu = document.getElementById('userMenu');
        const langBtn = document.getElementById('langBtn');
        const userLang = document.getElementById('userLang');
        
        if (!userMenuBtn || !userMenu) {
            console.warn('未找到用户菜单元素');
            return;
        }
        
        userMenuBtn.addEventListener('click', () => {
            userMenu.classList.toggle('show');
        });

        langBtn.addEventListener('click', () => {
            userLang.classList.toggle('show');
        });
        // 点击其他地方关闭菜单
        document.addEventListener('click', (e) => {
            if (!userMenuBtn.contains(e.target) && !userMenu.contains(e.target)) {
                userMenu.classList.remove('show');
            }
            if (!langBtn.contains(e.target) && !userLang.contains(e.target)) {
                userLang.classList.remove('show');
            }
        });

        document.getElementById('btn-zh').addEventListener('click', async () => {
            switchLang('zh');
            window.location.href = "/logout";
        });
        document.getElementById('btn-en').addEventListener('click', async () => {
            switchLang('en');
            window.location.href = "/logout";
        });

        // 绑定退出登录事件
        const logoutBtn = document.getElementById('logoutBtn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', () => {
                this.logout();
            });
        }
    }
    
    /**
     * 加载模块
     * @param {string} module - 模块名称
     */
    loadModule(module) {
        // 显示加载指示器
        this.showLoading(true);
        
        // 构建模块路径
        const modulePath = `views/${module}.html`;
        
        // 实际项目中应该使用fetch加载模块内容
        fetch(modulePath)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`请求失败: ${response.status} ${response.statusText}`);
                }
                return response.text();
            })
            .then(html => {
                // 更新内容容器 - 现在确保contentContainer存在
                this.contentContainer.innerHTML = html;
                
                // 更新活跃模块
                this.activeModule = module;
                
                // 更新导航状态
                this.updateNavState();

                // 执行模块初始化脚本
                this.initModuleScript(module);
                
                // 隐藏加载指示器
                this.showLoading(false);

                // 新增：模块加载完成后触发翻译
                if (renderSonLang && typeof renderSonLang === 'function') {
                    renderSonLang(currentUserLang);
                }

            })
            .catch(error => {
                console.error('加载模块失败:', error);
                this.contentContainer.innerHTML = `
                    <div class="alert alert-danger">
                        <strong>加载失败</strong>: 无法加载模块内容 (${error.message})
                    </div>
                `;
                this.showLoading(false);
            });
    }
    
    /**
     * 初始化模块脚本
     * @param {string} module - 模块名称
     */
    initModuleScript(module) {
        // 根据模块名称执行对应的初始化函数
        const initFunctions = {
            'dashboard': 'initDashboard',
            'devices': 'initDevices',
            'events': 'initEvents',
            'issues': 'initIssueManagement',
            'statistics': 'initStatistics',
            'user': 'initUserManagement',
            'customers': 'initCustomerManagement',
            'project': 'initProjectManagement'
        };
        
        const functionName = initFunctions[module];
        if (functionName && typeof window[functionName] === 'function') {
            try {
                window[functionName]();
            } catch (error) {
                console.error(`模块${module}初始化失败:`, error);
            }
        }
    }
    
    /**
     * 更新导航状态
     */
    updateNavState() {
        // 移除所有导航项的活跃状态
        this.navLinks.forEach(link => {
            link.classList.remove('active', 'bg-secondary');
        });
        
        // 为当前模块的导航项添加活跃状态
        const activeLink = document.querySelector(`.nav-link[data-module="${this.activeModule}"]`);
        if (activeLink) {
            activeLink.classList.add('active', 'bg-secondary');
        }
        
        // 更新页面标题
        if (this.pageTitleElement) {
            this.pageTitleElement.textContent = this.getModuleTitle(this.activeModule);
        }
    }
    
    /**
     * 获取模块标题
     * @param {string} module - 模块名称
     * @returns {string} - 模块标题
     */
    getModuleTitle(module) {
        const titles = {
            'dashboard': '首页概览',
            'devices': '设备管理',
            'events': '事件追踪',
            'issues': '事务管理',
            'statistics': '数据统计',
            'user': '用户管理',
            'about': '关于我们',
            'help': '帮助中心'
        };
        
        return titles[module] || '设备管理门户';
    }
    
    /**
     * 显示/隐藏加载指示器
     * @param {boolean} show - 是否显示
     */
    showLoading(show) {
        if (this.loadingIndicator) {
            this.loadingIndicator.style.display = show ? 'flex' : 'none';
        }
    }
    
    /**
     * 初始化通知提示框
     */
    initToast() {
        // 确保页面中有toast元素
        if (!document.getElementById('notificationToast')) {
            const toastHtml = `
                <div id="notificationToast" class="toast position-fixed bottom-5 right-5" role="alert" aria-live="assertive" aria-atomic="true">
                    <div class="toast-body bg-dark text-white">
                        <span id="toastMessage"></span>
                    </div>
                </div>
            `;
            document.body.insertAdjacentHTML('beforeend', toastHtml);
        }
    }
    
    /**
     * 显示提示消息
     * @param {string} message - 消息内容
     */
    showToast(message) {
        this.initToast();
        const toast = new bootstrap.Toast(document.getElementById('notificationToast'));
        document.getElementById('toastMessage').textContent = message;
        toast.show();
    }
    
    /**
     * 退出登录
     */
    logout() {
        if (confirm('确定要退出登录吗？')) {
            this.showLoading(true);

            window.location.href = "/logout";
        }
    }
}

function collectSonTexts() {
    const texts = [];
    const container = document.querySelector('.contentContainer');
    container.querySelectorAll(`.${config.translateClass}:not(.${config.ignoreClass})`).forEach(el => {
        const text = el.textContent.trim();
        if (text && !texts.includes(text)) {
            texts.push(text);
            // 确保原始文本被正确记录
            if (!el.dataset.originalText) {
                el.dataset.originalText = text;
            }
        }
    });
    return texts;
}

async function renderSonLang(targetLang) {
    // 更新html lang属性
    document.documentElement.lang = targetLang === 'zh' ? 'zh-CN' : 'en-US';

    await new Promise(resolve => setTimeout(resolve, 0));
    const texts = collectTexts();
    if (texts.length === 0) return;

    const transMap = await translate(texts, targetLang);

    const container = document.querySelector('.contentContainer');
    container.querySelectorAll(`.${config.translateClass}:not(.${config.ignoreClass})`).forEach(el => {
        if (!el.dataset.originalText) {
            el.dataset.originalText = el.textContent.trim();
        }
        const original = el.dataset.originalText;
        if (el.textContent.trim() !== transMap[original]) {
            el.textContent = transMap[original];
        }
        // const original = el.dataset.originalText || el.textContent.trim();
        // el.textContent = transMap[original] || original;
    });

    currentUserLang = targetLang;
}

/**
 * 格式化日期显示
 */
function formatDate(dateArray) {
    try {
        // 检查是否为数组且长度至少为3（年、月、日）
        if (!Array.isArray(dateArray) || dateArray.length < 3) {
            throw new Error('Invalid date array');
        }

        const [year, month, day, hour = 0, minute = 0, second = 0] = dateArray;

        // 创建日期对象
        const date = new Date(year, month - 1, day, hour, minute, second); // 月份需要减1

        // 检查日期是否有效
        if (isNaN(date.getTime())) {
            throw new Error('Invalid date');
        }

        // 格式化为 "YYYY-MM-DD HH:mm:ss"
        const formattedYear = date.getFullYear();
        const formattedMonth = String(date.getMonth() + 1).padStart(2, '0');
        const formattedDay = String(date.getDate()).padStart(2, '0');
        const formattedHours = String(date.getHours()).padStart(2, '0');
        const formattedMinutes = String(date.getMinutes()).padStart(2, '0');
        const formattedSeconds = String(date.getSeconds()).padStart(2, '0');

        return `${formattedYear}-${formattedMonth}-${formattedDay} ${formattedHours}:${formattedMinutes}:${formattedSeconds}`;
    } catch (error) {
        console.error('数组日期格式化错误:', error);
        // return JSON.stringify(dateArray); // 如果格式化失败，返回原始数组的字符串表示
        return "--";
    }
}

// 页面加载完成后初始化应用
document.addEventListener('DOMContentLoaded', () => {
    // 确保DOM完全加载后再初始化
    window.app = new AppController();
});
    