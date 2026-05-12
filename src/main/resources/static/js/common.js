/**
 * 校园活动报名与场地调度系统 - 公共JS
 */

const API_BASE = '/api';

// ========== HTTP 请求封装 ==========
async function request(url, options = {}) {
    const config = { headers: { 'Content-Type': 'application/json' }, ...options };
    try {
        const resp = await fetch(url, config);
        const data = await resp.json();
        if (data.code === 401 && !window.location.pathname.includes('/login')) { window.location.href = '/pages/login.html'; return null; }
        return data;
    } catch (err) { console.error('请求失败:', err); return { code: 500, message: '网络错误' }; }
}

const api = {
    get:    (url)            => request(url),
    post:   (url, body)      => request(url, { method: 'POST', body: JSON.stringify(body) }),
    put:    (url, body)      => request(url, { method: 'PUT',  body: JSON.stringify(body) }),
    del:    (url)            => request(url, { method: 'DELETE' }),
};

async function checkLogin() {
    const data = await api.get(API_BASE + '/person/current');
    if (!data || data.code !== 200) { window.location.href = '/pages/login.html'; return null; }
    return data.data;
}

async function logout() {
    await api.post(API_BASE + '/person/logout');
    localStorage.removeItem('campus_last_role');
    window.location.href = '/pages/login.html';
}

// ========== 提示 ==========
function showToast(message, type = 'success') {
    const bg = type === 'success' ? '#0f9d58' : type === 'error' ? '#db4437' : '#1a73e8';
    const toast = document.createElement('div');
    toast.style.cssText = `position:fixed;top:20px;right:20px;z-index:9999;background:${bg};color:#fff;padding:12px 24px;border-radius:8px;font-weight:500;animation:slideIn .3s ease;`;
    toast.textContent = message;
    document.body.appendChild(toast);
    setTimeout(() => { toast.style.opacity='0'; toast.style.transition='opacity .3s'; setTimeout(()=>toast.remove(),300); }, 2500);
}
const animStyle = document.createElement('style');
animStyle.textContent = '@keyframes slideIn{from{transform:translateX(100%);opacity:0}to{transform:translateX(0);opacity:1}}';
document.head.appendChild(animStyle);

// ========== 工具函数 ==========
function formatDate(dateStr) { if(!dateStr)return''; const d=new Date(dateStr);return d.toLocaleDateString('zh-CN'); }
function formatDateTime(dateStr) { if(!dateStr)return''; const d=new Date(dateStr);return d.toLocaleString('zh-CN'); }
function statusBadge(status) {
    const map={'待审核':'badge-pending','已通过':'badge-approved','已拒绝':'badge-rejected','已报名':'badge-approved','已取消':'badge-rejected'};
    const cls=map[status]||'bg-secondary';
    return `<span class="badge ${cls}">${status}</span>`;
}
function escapeHtml(str) { if(!str)return''; const d=document.createElement('div');d.textContent=str;return d.innerHTML; }

// ========== 分页控件 ==========
function renderPagination(total, page, pageSize, onPageChange) {
    const totalPages = Math.ceil(total / pageSize);
    if (totalPages <= 1) return '';
    let html = '<div class="pagination-wrapper"><ul class="pagination justify-content-center mb-0">';

    // 上一页
    html += `<li class="page-item ${page<=1?'disabled':''}">
        <a class="page-link" href="#" onclick="return false" data-pg="${page-1}">«</a></li>`;

    // 页码
    const start = Math.max(1, page - 2);
    const end = Math.min(totalPages, page + 2);
    if (start > 1) html += `<li class="page-item"><a class="page-link" href="#" onclick="return false" data-pg="1">1</a></li>`;
    if (start > 2) html += '<li class="page-item disabled"><span class="page-link">...</span></li>';
    for (let i = start; i <= end; i++) {
        html += `<li class="page-item ${i===page?'active':''}">
            <a class="page-link" href="#" onclick="return false" data-pg="${i}">${i}</a></li>`;
    }
    if (end < totalPages - 1) html += '<li class="page-item disabled"><span class="page-link">...</span></li>';
    if (end < totalPages) html += `<li class="page-item"><a class="page-link" href="#" onclick="return false" data-pg="${totalPages}">${totalPages}</a></li>`;

    // 下一页
    html += `<li class="page-item ${page>=totalPages?'disabled':''}">
        <a class="page-link" href="#" onclick="return false" data-pg="${page+1}">»</a></li>`;

    html += `<li class="page-item disabled"><span class="page-link text-muted" style="border:none;background:transparent;">共${total}条 ${page}/${totalPages}页</span></li>`;
    html += '</ul></div>';

    // 延迟绑定事件
    setTimeout(() => {
        document.querySelectorAll('.pagination-wrapper .page-link[data-pg]').forEach(link => {
            link.addEventListener('click', function(e) {
                e.preventDefault();
                const pg = parseInt(this.getAttribute('data-pg'));
                if (pg && pg !== page) onPageChange(pg);
            });
        });
    }, 0);

    return html;
}

// 已废弃别名兼容
const api_delete = api.del;
