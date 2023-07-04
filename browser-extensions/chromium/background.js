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
    console.log("click menu")
    if (info.menuItemId === "SourceDownloaderMenu") {
        // 执行send.js脚本
        chrome.tabs.executeScript(tab.id, {file: "send.js"});
    }
});