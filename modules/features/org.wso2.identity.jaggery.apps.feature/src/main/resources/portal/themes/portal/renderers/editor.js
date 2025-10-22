var render = function (theme, data, meta, require) {
    //print(caramel.build(data));
    theme('1-column-fluid', {
        title: data.title,
        navigation: [
            {
                partial: 'navigation',
                context: data.navigation
            }
        ],
        body: [
            {
                partial: 'portal-editor',
                context: data['portal-editor']
            }
        ]
    });
};