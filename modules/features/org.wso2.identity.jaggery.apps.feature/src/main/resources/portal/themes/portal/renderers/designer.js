var render = function (theme, data, meta, require) {
    theme('1-column-designer', {
        title: data.title,
        navigation: [
            {
                partial: 'navigation',
                context: data.navigation
            }
        ],
        body: [
            {
                partial: 'portal-dashboard-designer',
                context: data['portal-dashboard-designer']
            }
        ]
    });
};