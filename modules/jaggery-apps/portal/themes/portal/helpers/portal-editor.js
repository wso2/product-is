var resources = function (page, meta) {
    return {
        template: 'portal-editor.hbs',
        js: ['jquery-ui-1.10.1.custom.min.js', 'jquery.nicescroll.min.js', 'elfinder.full.js','portal-gadgets-js.jag','shindig.js', 'UESContainer.js',
            'codemirror/lib/codemirror.js','codemirror/search/searchcursor.js', 'codemirror/mode/xml.js', 'codemirror/mode/javascript.js',
            'codemirror/mode/css.js', 'codemirror/mode/htmlmixed.js', 'codemirror/hint/show-hint.js',
            'codemirror/hint/javascript-hint.js', 'codemirror/hint/html-hint.js', 'codemirror/mode/htmlembedded.js',
            'codemirror/addon/formatting.js', 'gadgetInclude.js','gadgetContainer.js','diff/diff.js',
            'autosave.js','portal-editor.js', 'jquery.validate.js', 'alert.js'
        ],
        css: [//'bootstrap-responsive.min.css', 'bootstrap.css'],
            'elfinderTheme.css', 'elfinder.min.css' , 'theme.css','portal-editor.css',
            'codemirror/codemirror.css', 'codemirror/show-hint.css'
        ],
        code: []
    };
};
