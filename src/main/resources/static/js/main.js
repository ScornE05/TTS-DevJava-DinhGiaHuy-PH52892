/**
 * Main JavaScript file for Staff Management System
 */

// Enable Bootstrap tooltips
document.addEventListener('DOMContentLoaded', function() {
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function(tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Enable Bootstrap popovers
    var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    var popoverList = popoverTriggerList.map(function(popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });

    // Auto-hide alerts after 5 seconds
    setTimeout(function() {
        var alerts = document.querySelectorAll('.alert:not(.alert-permanent)');
        alerts.forEach(function(alert) {
            var bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        });
    }, 5000);

    // Add confirm dialog to any element with data-confirm attribute
    document.querySelectorAll('[data-confirm]').forEach(function(element) {
        element.addEventListener('click', function(e) {
            if (!confirm(this.getAttribute('data-confirm'))) {
                e.preventDefault();
            }
        });
    });

    // Add active class to current navigation item
    var currentLocation = window.location.pathname;
    document.querySelectorAll('.nav-link').forEach(function(link) {
        if (link.getAttribute('href') === currentLocation) {
            link.classList.add('active');
        }
    });
});

// Function to initialize DataTables
function initDataTable(selector, options) {
    options = options || {};
    var defaultOptions = {
        language: {
            search: "Tìm kiếm:",
            lengthMenu: "Hiển thị _MENU_ dòng",
            info: "Hiển thị _START_ đến _END_ của _TOTAL_ dòng",
            infoEmpty: "Hiển thị 0 đến 0 của 0 dòng",
            infoFiltered: "(lọc từ _MAX_ dòng)",
            paginate: {
                first: "Đầu",
                last: "Cuối",
                next: "Sau",
                previous: "Trước"
            },
            emptyTable: "Không có dữ liệu",
            zeroRecords: "Không tìm thấy kết quả phù hợp"
        },
        responsive: true,
        pageLength: 10,
        lengthMenu: [[5, 10, 25, 50, -1], [5, 10, 25, 50, "Tất cả"]]
    };

    // Merge default options with custom options
    for (var key in options) {
        defaultOptions[key] = options[key];
    }

    return $(selector).DataTable(defaultOptions);
}

// Function to format dates
function formatDate(date) {
    if (!date) return '';

    if (typeof date === 'string') {
        date = new Date(date);
    }

    return new Intl.DateTimeFormat('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    }).format(date);
}

// Function to handle form validation
function validateForm(formSelector) {
    var form = document.querySelector(formSelector);
    if (!form) return false;

    if (!form.checkValidity()) {
        event.preventDefault();
        event.stopPropagation();
    }

    form.classList.add('was-validated');
    return form.checkValidity();
}

// Function to show a toast notification
function showToast(message, type) {
    type = type || 'info';
    var toastContainer = document.getElementById('toast-container');

    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.id = 'toast-container';
        toastContainer.className = 'position-fixed bottom-0 end-0 p-3';
        document.body.appendChild(toastContainer);
    }

    var toastId = 'toast-' + Date.now();
    var toastHtml = `
        <div id="${toastId}" class="toast align-items-center text-white bg-${type}" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="d-flex">
                <div class="toast-body">
                    ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    `;

    toastContainer.insertAdjacentHTML('beforeend', toastHtml);
    var toastElement = document.getElementById(toastId);
    var toast = new bootstrap.Toast(toastElement, { autohide: true, delay: 5000 });
    toast.show();

    return toast;
}

// Function to confirm delete action
function confirmDelete(message, callback) {
    message = message || 'Bạn có chắc chắn muốn xóa mục này?';

    if (confirm(message)) {
        if (typeof callback === 'function') {
            callback();
        }
        return true;
    }

    return false;
}