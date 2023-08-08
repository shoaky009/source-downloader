chrome.runtime.onInstalled.addListener(function () {
    // 创建右键菜单项
    chrome.contextMenus.create({
        id: "SourceDownloaderMenu",
        title: "Send to processor",
        contexts: ["all"]
    });
});

// 监听右键菜单点击事件
chrome.contextMenus.onClicked.addListener(function (info, tab) {
    if (info.menuItemId === "SourceDownloaderMenu") {
        console.log("execute")
        // current tab url

        // url match script config

        // if not match, return

        // execute each script

        // send to processor

    }
});