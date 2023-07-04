document.addEventListener('DOMContentLoaded', function () {
    // 获取页面元素
    const serverUrlInput = document.getElementById('server-url-input');
    const saveButton = document.getElementById('save-button');
    const testButton = document.getElementById('test-button');
    const statusMessage = document.getElementById('status-message');

    // 从Chrome存储中加载配置
    chrome.storage.sync.get('serverUrl', function (data) {
        const savedServerUrl = data.serverUrl;
        if (savedServerUrl) {
            serverUrlInput.value = savedServerUrl;
        }
    });

    // 保存配置
    saveButton.addEventListener('click', function () {
        const serverUrl = serverUrlInput.value;
        chrome.storage.sync.set({serverUrl: serverUrl}, function () {
            statusMessage.textContent = '配置已保存';
        });
    });

    // 测试连接
    testButton.addEventListener('click', function () {
        const serverUrl = serverUrlInput.value;
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