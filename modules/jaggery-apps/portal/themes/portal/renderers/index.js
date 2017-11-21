var render = function (theme, data, meta, require) {
    //print(caramel.build(data));
    theme('1-column', {
        title: data.title,
        navigation: [
            {
                partial: 'navigation',
                context: data.navigation
            }
        ],
        body: [
            {
                partial: 'portal-homepage',
                context: data['portal-homepage']
            }
        ]
    });
};