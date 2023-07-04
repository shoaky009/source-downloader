document.addEventListener('DOMContentLoaded', function () {
    // 获取页面元素
    const serverUrlInput = document.getElementById('server-url-input');
    const saveButton = document.getElementById('save-button');
    const testButton = document.getElementById('test-button');
    const statusMessage = document.getElementById('status-message');

    // 从Chrome存储中加载配置
    chrome.storage.sync.get('serverUrl', function (data) {
        var savedServerUrl = data.serverUrl;
        if (savedServerUrl) {
            serverUrlInput.value = savedServerUrl;
        }
    });

    // 保存配置
    saveButton.addEventListener('click', function () {
        const serverUrl = serverUrlInput.value;

        // 将配置保存到Chrome存储中
        chrome.storage.sync.set({serverUrl: serverUrl}, function () {
            // 显示保存成功的消息
            statusMessage.textContent = '配置已保存';
        });
    });

    // 测试连接
    testButton.addEventListener('click', function () {
        const serverUrl = serverUrlInput.value;
        // 发起与服务的连接测试请求
        // 这里可以使用XMLHttpRequest或者fetch等方法进行请求

        // 示例：使用fetch方法测试与服务的连通性
        fetch(serverUrl + "/api/application/info")
            .then(function (response) {
                if (response.ok) {
                    statusMessage.textContent = '连接成功';
                } else {
                    statusMessage.textContent = '连接失败';
                }
            })
            .catch(function (error) {
                statusMessage.textContent = '连接错误: ' + error.message;
            });
    });
});