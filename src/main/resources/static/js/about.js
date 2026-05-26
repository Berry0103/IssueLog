// 初始化关于我们页面
window.initAbout = function() {
    // 团队成员卡片悬停效果增强
    const teamMembers = document.querySelectorAll('.team-member');
    teamMembers.forEach(member => {
        member.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-10px)';
            this.style.boxShadow = '0 15px 30px rgba(0, 0, 0, 0.1)';
        });
        
        member.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = '0 2px 10px rgba(0, 0, 0, 0.05)';
        });
    });
    
    // 联系信息点击事件
    const contactItems = document.querySelectorAll('.contact-item');
    contactItems.forEach(item => {
        item.addEventListener('click', function() {
            const contactInfo = this.querySelector('span').textContent;
            const contactType = this.querySelector('i').classList.contains('bi-envelope') ? '邮箱' : 
                               this.querySelector('i').classList.contains('bi-phone') ? '电话' :
                               this.querySelector('i').classList.contains('bi-map-marker') ? '地址' : '工作时间';
            
            if (contactType === '邮箱') {
                window.location.href = `mailto:${contactInfo}`;
            } else if (contactType === '电话') {
                window.showToast(`拨打 ${contactInfo}`);
            } else if (contactType === '地址') {
                window.showToast(`查看地图: ${contactInfo}`);
            } else {
                window.showToast(`工作时间: ${contactInfo}`);
            }
        });
    });
};
