/**
 * 认证管理类 - 处理登录验证与会话管理
 */
class AuthManager {
    constructor() {
        // 初始化认证状态
        this.isAuthenticated = this.checkAuthStatus();
        this.currentUser = this.getCurrentUser();
        
        // 绑定事件
        this.bindEvents();
    }
    
    /**
     * 检查认证状态
     */
    checkAuthStatus() {
        const user = localStorage.getItem('currentUser');
        const sessionId = localStorage.getItem('sessionId');
        return !!user && !!sessionId;
    }
    
    /**
     * 获取当前用户信息
     */
    getCurrentUser() {
        const user = localStorage.getItem('currentUser');
        return user ? JSON.parse(user) : null;
    }
    
    /**
     * 绑定认证相关事件
     */
    bindEvents() {
        // 登录按钮点击事件
        document.addEventListener('click', (e) => {
            if (e.target.closest('#loginBtn')) {
                this.login();
            } else if (e.target.closest('#logoutBtn')) {
                this.logout();
            } else if (e.target.closest('#goToLoginBtn')) {
                this.showLoginForm();
            }
        });
        
        // 验证码刷新
        document.addEventListener('click', (e) => {
            if (e.target.closest('#refreshCaptcha')) {
                this.generateCaptcha();
            }
        });
    }
    
    /**
     * 显示登录表单
     */
    showLoginForm() {
        const contentContainer = document.getElementById('contentContainer');
        if (contentContainer) {
            contentContainer.innerHTML = `
                <div class="login-container">
                    <div class="login-card">
                        <div class="text-center mb-4">
                            <img src="./image/logo.png" alt="Logo" class="login-logo">
                            <h2 class="gradient-text mt-2">设备管理门户</h2>
                            <p>请登录以继续使用系统</p>
                        </div>
                        
                        <form id="loginForm">
                            <div class="mb-3">
                                <label for="username" class="form-label">用户名</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fa fa-user"></i></span>
                                    <input type="text" class="form-control" id="username" required placeholder="请输入用户名">
                                </div>
                            </div>
                            
                            <div class="mb-3">
                                <label for="password" class="form-label">密码</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fa fa-lock"></i></span>
                                    <input type="password" class="form-control" id="password" required placeholder="请输入密码">
                                </div>
                            </div>
                            
                            <div class="mb-3">
                                <label for="captcha" class="form-label">验证码</label>
                                <div class="input-group">
                                    <input type="text" class="form-control" id="captcha" required placeholder="请输入验证码">
                                    <div class="captcha-container input-group-text">
                                        <span id="captchaCode">ABCD</span>
                                        <button type="button" id="refreshCaptcha" class="btn btn-sm ms-2">
                                            <i class="fa fa-refresh"></i>
                                        </button>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="mb-3 form-check">
                                <input type="checkbox" class="form-check-input" id="rememberMe">
                                <label class="form-check-label" for="rememberMe">记住我</label>
                            </div>
                            
                            <button type="submit" class="btn btn-primary w-100" id="loginBtn">
                                <i class="fa fa-sign-in me-2"></i>登录
                            </button>
                            
                            <div id="loginError" class="alert alert-danger mt-3 d-none"></div>
                        </form>
                    </div>
                </div>
            `;
            
            // 生成验证码
            this.generateCaptcha();
        }
    }
    
    /**
     * 生成随机验证码
     */
    generateCaptcha() {
        const chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ';
        let captcha = '';
        for (let i = 0; i < 4; i++) {
            captcha += chars.charAt(Math.floor(Math.random() * chars.length));
        }
        
        const captchaElement = document.getElementById('captchaCode');
        if (captchaElement) {
            captchaElement.textContent = captcha;
        }
    }
    
    /**
     * 登录处理
     */
    login() {
        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;
        const captcha = document.getElementById('captcha').value.trim();
        const captchaCode = document.getElementById('captchaCode').textContent;
        const errorElement = document.getElementById('loginError');
        
        // 验证码验证
        if (captcha.toUpperCase() !== captchaCode) {
            this.showError(errorElement, '验证码不正确，请重新输入');
            return;
        }
        
        // 发送登录请求到后端
        this.authenticateWithServer(username, password)
            .then(response => {
                if (response.success) {
                    // 登录成功，保存用户信息和会话
                    localStorage.setItem('currentUser', JSON.stringify(response.user));
                    localStorage.setItem('sessionId', response.sessionId);
                    
                    // 更新认证状态
                    this.isAuthenticated = true;
                    this.currentUser = response.user;
                    
                    // 显示成功消息并刷新页面
                    app.showToast('登录成功，欢迎回来！');
                    setTimeout(() => {
                        window.location.reload();
                    }, 1000);
                } else {
                    // 显示错误消息
                    this.showError(errorElement, response.message);
                }
            })
            .catch(error => {
                this.showError(errorElement, '登录失败，请稍后重试');
                console.error('登录请求失败:', error);
            });
    }
    
    /**
     * 与服务器验证用户凭据
     * @param {string} username - 用户名
     * @param {string} password - 密码
     * @returns {Promise} - 包含验证结果的Promise
     */
    authenticateWithServer(username, password) {
        // 这里模拟后端验证过程
        // 实际项目中应替换为真实的API请求
        return new Promise((resolve) => {
            // 模拟API请求延迟
            setTimeout(() => {
                // 模拟数据库查询过程
                // 实际项目中这部分逻辑在后端实现，查询MySQL数据库
                if (!username) {
                    return resolve({
                        success: false,
                        message: '请输入用户名'
                    });
                }
                
                // 模拟查询hub_user表检查用户名是否存在
                // 实际SQL: SELECT * FROM hub_user WHERE username = ?
                if (!this.isUsernameExists(username)) {
                    return resolve({
                        success: false,
                        message: '用户名不存在'
                    });
                }
                
                // 模拟查询hub_user表检查密码是否正确
                // 实际SQL: SELECT * FROM hub_user WHERE username = ? AND password = ?
                if (!this.isPasswordCorrect(username, password)) {
                    return resolve({
                        success: false,
                        message: '密码不正确'
                    });
                }
                
                // 登录成功，获取用户信息和角色信息
                // 实际SQL: SELECT u.*, r.role_name FROM hub_user u 
                //          LEFT JOIN hub_role r ON u.role_id = r.id 
                //          WHERE u.username = ?
                const user = this.getUserInfo(username);
                
                // 返回成功结果
                resolve({
                    success: true,
                    message: '登录成功',
                    user: user,
                    sessionId: this.generateSessionId()
                });
            }, 800);
        });
    }
    
    /**
     * 登出处理
     */
    logout() {
        if (confirm('确定要退出登录吗？')) {
            // 清除本地存储
            localStorage.removeItem('currentUser');
            localStorage.removeItem('sessionId');
            
            // 更新认证状态
            this.isAuthenticated = false;
            this.currentUser = null;
            
            // 显示消息并刷新页面
            app.showToast('已成功退出登录');
            setTimeout(() => {
                window.location.reload();
            }, 800);
        }
    }
    
    /**
     * 显示未登录提示
     */
    showLoginPrompt() {
        const contentContainer = document.getElementById('contentContainer');
        if (contentContainer) {
            contentContainer.innerHTML = `
                <div class="login-prompt">
                    <div class="prompt-card text-center">
                        <div class="prompt-icon">
                            <i class="fa fa-user-circle"></i>
                        </div>
                        <h2>请先登录</h2>
                        <p class="mb-4">您需要登录后才能使用设备管理门户的功能</p>
                        <button id="goToLoginBtn" class="btn btn-primary">
                            <i class="fa fa-sign-in me-2"></i>前往登录
                        </button>
                    </div>
                </div>
            `;
        }
        
        // 隐藏不需要的元素
        document.querySelector('.search-container')?.classList.add('d-none');
        document.querySelector('.navbar-right .btn-outline-light.position-relative')?.classList.add('d-none');
        document.getElementById('userMenuBtn')?.classList.add('d-none');
    }
    
    /**
     * 显示错误消息
     * @param {HTMLElement} element - 错误消息元素
     * @param {string} message - 错误消息内容
     */
    showError(element, message) {
        if (element) {
            element.textContent = message;
            element.classList.remove('d-none');
            
            // 3秒后自动隐藏错误消息
            setTimeout(() => {
                element.classList.add('d-none');
            }, 3000);
        }
    }
    
    /**
     * 生成会话ID
     */
    generateSessionId() {
        return 'sid_' + Math.random().toString(36).substring(2, 15) + 
               Math.random().toString(36).substring(2, 15);
    }
    
    // 以下方法仅为前端模拟，实际应在后端实现
    
    /**
     * 模拟检查用户名是否存在（实际应查询hub_user表）
     * @param {string} username - 用户名
     * @returns {boolean} - 是否存在
     */
    isUsernameExists(username) {
        // 模拟数据库中的用户
        const validUsers = ['admin', 'operator', 'viewer'];
        return validUsers.includes(username);
    }
    
    /**
     * 模拟检查密码是否正确（实际应查询hub_user表）
     * @param {string} username - 用户名
     * @param {string} password - 密码
     * @returns {boolean} - 是否正确
     */
    isPasswordCorrect(username, password) {
        // 模拟数据库中的用户密码（实际应存储加密后的密码）
        const userPasswords = {
            'admin': 'admin123',
            'operator': 'operator123',
            'viewer': 'viewer123'
        };
        return userPasswords[username] === password;
    }
    
    /**
     * 模拟获取用户信息（实际应查询hub_user和hub_role表）
     * @param {string} username - 用户名
     * @returns {object} - 用户信息
     */
    getUserInfo(username) {
        // 模拟从数据库获取用户和角色信息
        const userRoles = {
            'admin': { id: 1, role_name: '管理员' },
            'operator': { id: 2, role_name: '操作员' },
            'viewer': { id: 3, role_name: '查看者' }
        };
        
        return {
            id: Math.floor(Math.random() * 1000),
            username: username,
            name: username === 'admin' ? '系统管理员' : 
                  username === 'operator' ? '设备操作员' : '数据查看员',
            role: userRoles[username],
            lastLogin: new Date().toLocaleString()
        };
    }
}

// 页面加载完成后初始化认证管理器
document.addEventListener('DOMContentLoaded', () => {
    // 等待主应用初始化完成
    const initAuth = () => {
        if (window.app) {
            window.auth = new AuthManager();
            
            // 检查登录状态
            if (!auth.isAuthenticated) {
                auth.showLoginPrompt();
            } else {
                // 登录状态下更新用户显示
                const userNameElement = document.querySelector('#userMenuBtn span');
                if (userNameElement && auth.currentUser) {
                    userNameElement.textContent = auth.currentUser.name;
                }
            }
        } else {
            setTimeout(initAuth, 100);
        }
    };
    
    initAuth();
});
    