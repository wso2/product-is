var render = function (theme, data, meta, require) {
    //print(caramel.build(data));
    theme('error', {
        title: data.title,
       
        body: [
            {
                partial: 'error-404',
                context: {}
            }
        ]
    });
};