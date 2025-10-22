UESContainer.renderGadget('gadgetAreaIntro-0', portalGadgets.intro0);
UESContainer.renderGadget('gadgetAreaIntro-1', portalGadgets.intro1, {}, function () {
    $($('iframe').get(0)).load(function () {
        //this function is wrapped this way to delay the data load until the
        // gadget is loaded in Firefox
        setTimeout('updateIntroGadgets(1)', 2000);
    });
});

var data = [
    [
        [
            {"indicator": {"id": "BX.GSR.GNFS.CD", "value": "Exports of goods and services (BoP, current US$)"}, "country": {"id": "JP", "value": "Japan"}, "value": "927300000000", "decimal": "0", "date": "2011"},
            {"indicator": {"id": "BX.GSR.GNFS.CD", "value": "Exports of goods and services (BoP, current US$)"}, "country": {"id": "JP", "value": "Japan"}, "value": "866652000000", "decimal": "0", "date": "2010"},
            {"indicator": {"id": "BX.GSR.GNFS.CD", "value": "Exports of goods and services (BoP, current US$)"}, "country": {"id": "JP", "value": "Japan"}, "value": "669059000000", "decimal": "0", "date": "2009"},
            {"indicator": {"id": "BX.GSR.GNFS.CD", "value": "Exports of goods and services (BoP, current US$)"}, "country": {"id": "JP", "value": "Japan"}, "value": "890158000000", "decimal": "0", "date": "2008"}
        ],
        [
            {"indicator": {"id": "NY.GDP.PCAP.CD", "value": "GDP per capita (current US$)"}, "country": {"id": "JP", "value": "Japan"}, "value": "46134.5682388357", "decimal": "0", "date": "2011"},
            {"indicator": {"id": "NY.GDP.PCAP.CD", "value": "GDP per capita (current US$)"}, "country": {"id": "JP", "value": "Japan"}, "value": "43117.7682732757", "decimal": "0", "date": "2010"},
            {"indicator": {"id": "NY.GDP.PCAP.CD", "value": "GDP per capita (current US$)"}, "country": {"id": "JP", "value": "Japan"}, "value": "39473.3629058165", "decimal": "0", "date": "2009"},
            {"indicator": {"id": "NY.GDP.PCAP.CD", "value": "GDP per capita (current US$)"}, "country": {"id": "JP", "value": "Japan"}, "value": "37972.2372128835", "decimal": "0", "date": "2008"}
        ]
    ],
    [
        [
            {"indicator": {"id": "BX.GSR.GNFS.CD", "value": "Exports of goods and services (BoP, current US$)"}, "country": {"id": "AU", "value": "Australia"}, "value": "323333000000", "decimal": "0", "date": "2011"},
            {"indicator": {"id": "BX.GSR.GNFS.CD", "value": "Exports of goods and services (BoP, current US$)"}, "country": {"id": "AU", "value": "Australia"}, "value": "260750000000", "decimal": "0", "date": "2010"},
            {"indicator": {"id": "BX.GSR.GNFS.CD", "value": "Exports of goods and services (BoP, current US$)"}, "country": {"id": "AU", "value": "Australia"}, "value": "195591000000", "decimal": "0", "date": "2009"},
            {"indicator": {"id": "BX.GSR.GNFS.CD", "value": "Exports of goods and services (BoP, current US$)"}, "country": {"id": "AU", "value": "Australia"}, "value": "233499000000", "decimal": "0", "date": "2008"}
        ],
        [
            {"indicator": {"id": "NY.GDP.PCAP.CD", "value": "GDP per capita (current US$)"}, "country": {"id": "AU", "value": "Australia"}, "value": "62002.8437767099", "decimal": "0", "date": "2011"},
            {"indicator": {"id": "NY.GDP.PCAP.CD", "value": "GDP per capita (current US$)"}, "country": {"id": "AU", "value": "Australia"}, "value": "51586.0873491532", "decimal": "0", "date": "2010"},
            {"indicator": {"id": "NY.GDP.PCAP.CD", "value": "GDP per capita (current US$)"}, "country": {"id": "AU", "value": "Australia"}, "value": "42403.5958882455", "decimal": "0", "date": "2009"},
            {"indicator": {"id": "NY.GDP.PCAP.CD", "value": "GDP per capita (current US$)"}, "country": {"id": "AU", "value": "Australia"}, "value": "49206.6811398398", "decimal": "0", "date": "2008"}
        ]
    ],
    [
        [
            {"indicator": {"id": "BX.GSR.GNFS.CD", "value": "Exports of goods and services (BoP, current US$)"}, "country": {"id": "US", "value": "United States"}, "value": "2104900000000", "decimal": "0", "date": "2011"},
            {"indicator": {"id": "BX.GSR.GNFS.CD", "value": "Exports of goods and services (BoP, current US$)"}, "country": {"id": "US", "value": "United States"}, "value": "1844120000000", "decimal": "0", "date": "2010"},
            {"indicator": {"id": "BX.GSR.GNFS.CD", "value": "Exports of goods and services (BoP, current US$)"}, "country": {"id": "US", "value": "United States"}, "value": "1581660000000", "decimal": "0", "date": "2009"},
            {"indicator": {"id": "BX.GSR.GNFS.CD", "value": "Exports of goods and services (BoP, current US$)"}, "country": {"id": "US", "value": "United States"}, "value": "1845170000000", "decimal": "0", "date": "2008"}
        ],
        [
            {"indicator": {"id": "NY.GDP.PCAP.CD", "value": "GDP per capita (current US$)"}, "country": {"id": "US", "value": "United States"}, "value": "48112.6001409503", "decimal": "0", "date": "2011"},
            {"indicator": {"id": "NY.GDP.PCAP.CD", "value": "GDP per capita (current US$)"}, "country": {"id": "US", "value": "United States"}, "value": "46615.5108575097", "decimal": "0", "date": "2010"},
            {"indicator": {"id": "NY.GDP.PCAP.CD", "value": "GDP per capita (current US$)"}, "country": {"id": "US", "value": "United States"}, "value": "45305.0517605237", "decimal": "0", "date": "2009"},
            {"indicator": {"id": "NY.GDP.PCAP.CD", "value": "GDP per capita (current US$)"}, "country": {"id": "US", "value": "United States"}, "value": "46759.5598394741", "decimal": "0", "date": "2008"}
        ]
    ],
    [
        [
            {"indicator": {"id": "BX.GSR.GNFS.CD", "value": "Exports of goods and services (BoP, current US$)"}, "country": {"id": "CN", "value": "China"}, "value": "1990660000000", "decimal": "0", "date": "2011"},
            {"indicator": {"id": "BX.GSR.GNFS.CD", "value": "Exports of goods and services (BoP, current US$)"}, "country": {"id": "CN", "value": "China"}, "value": "1647720000000", "decimal": "0", "date": "2010"},
            {"indicator": {"id": "BX.GSR.GNFS.CD", "value": "Exports of goods and services (BoP, current US$)"}, "country": {"id": "CN", "value": "China"}, "value": "1260330000000", "decimal": "0", "date": "2009"},
            {"indicator": {"id": "BX.GSR.GNFS.CD", "value": "Exports of goods and services (BoP, current US$)"}, "country": {"id": "CN", "value": "China"}, "value": "1493570000000", "decimal": "0", "date": "2008"}
        ],
        [
            {"indicator": {"id": "NY.GDP.PCAP.CD", "value": "GDP per capita (current US$)"}, "country": {"id": "CN", "value": "China"}, "value": "5447.34142164051", "decimal": "0", "date": "2011"},
            {"indicator": {"id": "NY.GDP.PCAP.CD", "value": "GDP per capita (current US$)"}, "country": {"id": "CN", "value": "China"}, "value": "4433.36121999931", "decimal": "0", "date": "2010"},
            {"indicator": {"id": "NY.GDP.PCAP.CD", "value": "GDP per capita (current US$)"}, "country": {"id": "CN", "value": "China"}, "value": "3749.27242367005", "decimal": "0", "date": "2009"},
            {"indicator": {"id": "NY.GDP.PCAP.CD", "value": "GDP per capita (current US$)"}, "country": {"id": "CN", "value": "China"}, "value": "3413.58866142806", "decimal": "0", "date": "2008"}
        ]
    ]
];

var updateIntroGadgets = function (j) {
    j = parseInt(j) - 1;

    UESContainer.inlineClient.publish('org.uec.geo.intro1', data[j][0]);
    UESContainer.inlineClient.publish('org.uec.geo.intro2', data[j][1]);
};
