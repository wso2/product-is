var saveCodeMirror;
(function () {
    var timerUnlocked = true;

    saveCodeMirror = function () {
        if (timerUnlocked && window.getCodeMirrorArr) {
            var cmArr = window.getCodeMirrorArr();
            if (cmArr) {
                for (cmTabId in cmArr) {
                    var cmInfo = cmArr[cmTabId];
                    var icon = cmInfo.tab.find('.autosave');
                    if (cmInfo.changed) {
                        icon.removeClass('icon-asterisk');
                        icon.addClass('icon-spinner');
                        var newSave = cmInfo.cm.getValue();
                        cmInfo.changed = false;
                        timerUnlocked = false;
                        var patch = JsDiff.createPatch(cmInfo.hash, cmInfo.lastSaved, newSave);
                        $.ajax({
                            url: 'apis/autosave.jag',
                            data: { page: editor.site, diff: patch},
                            success: function (isSuccess) {
                                if (isSuccess.updated) {
                                    cmInfo.lastSaved = newSave;
                                    icon.removeClass('icon-spinner');
                                }
                            },
                            dataType: 'json'
                        });
                    }
                }
            }
        }
    };

    var autoSaveTimer = function () {
        timerUnlocked = true;
        saveCodeMirror();
        setTimeout(autoSaveTimer, 5000);
    };
    autoSaveTimer();


})();
