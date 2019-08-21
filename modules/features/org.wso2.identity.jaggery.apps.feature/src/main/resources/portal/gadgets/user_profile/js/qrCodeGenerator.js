// alignment pattern
adelta = [
  0, 11, 15, 19, 23, 27, 31, // force 1 pat
  16, 18, 20, 22, 24, 26, 28, 20, 22, 24, 24, 26, 28, 28, 22, 24, 24,
  26, 26, 28, 28, 24, 24, 26, 26, 26, 28, 28, 24, 26, 26, 26, 28, 28
  ];

// version block
vpat = [
    0xc94, 0x5bc, 0xa99, 0x4d3, 0xbf6, 0x762, 0x847, 0x60d,
    0x928, 0xb78, 0x45d, 0xa17, 0x532, 0x9a6, 0x683, 0x8c9,
    0x7ec, 0xec4, 0x1e1, 0xfab, 0x08e, 0xc1a, 0x33f, 0xd75,
    0x250, 0x9d5, 0x6f0, 0x8ba, 0x79f, 0xb0b, 0x42e, 0xa64,
    0x541, 0xc69
];

// final format bits with mask: level << 3 | mask
fmtword = [
    0x77c4, 0x72f3, 0x7daa, 0x789d, 0x662f, 0x6318, 0x6c41, 0x6976,    //L
    0x5412, 0x5125, 0x5e7c, 0x5b4b, 0x45f9, 0x40ce, 0x4f97, 0x4aa0,    //M
    0x355f, 0x3068, 0x3f31, 0x3a06, 0x24b4, 0x2183, 0x2eda, 0x2bed,    //Q
    0x1689, 0x13be, 0x1ce7, 0x19d0, 0x0762, 0x0255, 0x0d0c, 0x083b    //H
];

// 4 per version: number of blocks 1,2; data width; ecc width
eccblocks = [
    1, 0, 19, 7, 1, 0, 16, 10, 1, 0, 13, 13, 1, 0, 9, 17,
    1, 0, 34, 10, 1, 0, 28, 16, 1, 0, 22, 22, 1, 0, 16, 28,
    1, 0, 55, 15, 1, 0, 44, 26, 2, 0, 17, 18, 2, 0, 13, 22,
    1, 0, 80, 20, 2, 0, 32, 18, 2, 0, 24, 26, 4, 0, 9, 16,
    1, 0, 108, 26, 2, 0, 43, 24, 2, 2, 15, 18, 2, 2, 11, 22,
    2, 0, 68, 18, 4, 0, 27, 16, 4, 0, 19, 24, 4, 0, 15, 28,
    2, 0, 78, 20, 4, 0, 31, 18, 2, 4, 14, 18, 4, 1, 13, 26,
    2, 0, 97, 24, 2, 2, 38, 22, 4, 2, 18, 22, 4, 2, 14, 26,
    2, 0, 116, 30, 3, 2, 36, 22, 4, 4, 16, 20, 4, 4, 12, 24,
    2, 2, 68, 18, 4, 1, 43, 26, 6, 2, 19, 24, 6, 2, 15, 28,
    4, 0, 81, 20, 1, 4, 50, 30, 4, 4, 22, 28, 3, 8, 12, 24,
    2, 2, 92, 24, 6, 2, 36, 22, 4, 6, 20, 26, 7, 4, 14, 28,
    4, 0, 107, 26, 8, 1, 37, 22, 8, 4, 20, 24, 12, 4, 11, 22,
    3, 1, 115, 30, 4, 5, 40, 24, 11, 5, 16, 20, 11, 5, 12, 24,
    5, 1, 87, 22, 5, 5, 41, 24, 5, 7, 24, 30, 11, 7, 12, 24,
    5, 1, 98, 24, 7, 3, 45, 28, 15, 2, 19, 24, 3, 13, 15, 30,
    1, 5, 107, 28, 10, 1, 46, 28, 1, 15, 22, 28, 2, 17, 14, 28,
    5, 1, 120, 30, 9, 4, 43, 26, 17, 1, 22, 28, 2, 19, 14, 28,
    3, 4, 113, 28, 3, 11, 44, 26, 17, 4, 21, 26, 9, 16, 13, 26,
    3, 5, 107, 28, 3, 13, 41, 26, 15, 5, 24, 30, 15, 10, 15, 28,
    4, 4, 116, 28, 17, 0, 42, 26, 17, 6, 22, 28, 19, 6, 16, 30,
    2, 7, 111, 28, 17, 0, 46, 28, 7, 16, 24, 30, 34, 0, 13, 24,
    4, 5, 121, 30, 4, 14, 47, 28, 11, 14, 24, 30, 16, 14, 15, 30,
    6, 4, 117, 30, 6, 14, 45, 28, 11, 16, 24, 30, 30, 2, 16, 30,
    8, 4, 106, 26, 8, 13, 47, 28, 7, 22, 24, 30, 22, 13, 15, 30,
    10, 2, 114, 28, 19, 4, 46, 28, 28, 6, 22, 28, 33, 4, 16, 30,
    8, 4, 122, 30, 22, 3, 45, 28, 8, 26, 23, 30, 12, 28, 15, 30,
    3, 10, 117, 30, 3, 23, 45, 28, 4, 31, 24, 30, 11, 31, 15, 30,
    7, 7, 116, 30, 21, 7, 45, 28, 1, 37, 23, 30, 19, 26, 15, 30,
    5, 10, 115, 30, 19, 10, 47, 28, 15, 25, 24, 30, 23, 25, 15, 30,
    13, 3, 115, 30, 2, 29, 46, 28, 42, 1, 24, 30, 23, 28, 15, 30,
    17, 0, 115, 30, 10, 23, 46, 28, 10, 35, 24, 30, 19, 35, 15, 30,
    17, 1, 115, 30, 14, 21, 46, 28, 29, 19, 24, 30, 11, 46, 15, 30,
    13, 6, 115, 30, 14, 23, 46, 28, 44, 7, 24, 30, 59, 1, 16, 30,
    12, 7, 121, 30, 12, 26, 47, 28, 39, 14, 24, 30, 22, 41, 15, 30,
    6, 14, 121, 30, 6, 34, 47, 28, 46, 10, 24, 30, 2, 64, 15, 30,
    17, 4, 122, 30, 29, 14, 46, 28, 49, 10, 24, 30, 24, 46, 15, 30,
    4, 18, 122, 30, 13, 32, 46, 28, 48, 14, 24, 30, 42, 32, 15, 30,
    20, 4, 117, 30, 40, 7, 47, 28, 43, 22, 24, 30, 10, 67, 15, 30,
    19, 6, 118, 30, 18, 31, 47, 28, 34, 34, 24, 30, 20, 61, 15, 30
];

// Galois field log table
glog = [
    0xff, 0x00, 0x01, 0x19, 0x02, 0x32, 0x1a, 0xc6, 0x03, 0xdf, 0x33, 0xee, 0x1b, 0x68, 0xc7, 0x4b,
    0x04, 0x64, 0xe0, 0x0e, 0x34, 0x8d, 0xef, 0x81, 0x1c, 0xc1, 0x69, 0xf8, 0xc8, 0x08, 0x4c, 0x71,
    0x05, 0x8a, 0x65, 0x2f, 0xe1, 0x24, 0x0f, 0x21, 0x35, 0x93, 0x8e, 0xda, 0xf0, 0x12, 0x82, 0x45,
    0x1d, 0xb5, 0xc2, 0x7d, 0x6a, 0x27, 0xf9, 0xb9, 0xc9, 0x9a, 0x09, 0x78, 0x4d, 0xe4, 0x72, 0xa6,
    0x06, 0xbf, 0x8b, 0x62, 0x66, 0xdd, 0x30, 0xfd, 0xe2, 0x98, 0x25, 0xb3, 0x10, 0x91, 0x22, 0x88,
    0x36, 0xd0, 0x94, 0xce, 0x8f, 0x96, 0xdb, 0xbd, 0xf1, 0xd2, 0x13, 0x5c, 0x83, 0x38, 0x46, 0x40,
    0x1e, 0x42, 0xb6, 0xa3, 0xc3, 0x48, 0x7e, 0x6e, 0x6b, 0x3a, 0x28, 0x54, 0xfa, 0x85, 0xba, 0x3d,
    0xca, 0x5e, 0x9b, 0x9f, 0x0a, 0x15, 0x79, 0x2b, 0x4e, 0xd4, 0xe5, 0xac, 0x73, 0xf3, 0xa7, 0x57,
    0x07, 0x70, 0xc0, 0xf7, 0x8c, 0x80, 0x63, 0x0d, 0x67, 0x4a, 0xde, 0xed, 0x31, 0xc5, 0xfe, 0x18,
    0xe3, 0xa5, 0x99, 0x77, 0x26, 0xb8, 0xb4, 0x7c, 0x11, 0x44, 0x92, 0xd9, 0x23, 0x20, 0x89, 0x2e,
    0x37, 0x3f, 0xd1, 0x5b, 0x95, 0xbc, 0xcf, 0xcd, 0x90, 0x87, 0x97, 0xb2, 0xdc, 0xfc, 0xbe, 0x61,
    0xf2, 0x56, 0xd3, 0xab, 0x14, 0x2a, 0x5d, 0x9e, 0x84, 0x3c, 0x39, 0x53, 0x47, 0x6d, 0x41, 0xa2,
    0x1f, 0x2d, 0x43, 0xd8, 0xb7, 0x7b, 0xa4, 0x76, 0xc4, 0x17, 0x49, 0xec, 0x7f, 0x0c, 0x6f, 0xf6,
    0x6c, 0xa1, 0x3b, 0x52, 0x29, 0x9d, 0x55, 0xaa, 0xfb, 0x60, 0x86, 0xb1, 0xbb, 0xcc, 0x3e, 0x5a,
    0xcb, 0x59, 0x5f, 0xb0, 0x9c, 0xa9, 0xa0, 0x51, 0x0b, 0xf5, 0x16, 0xeb, 0x7a, 0x75, 0x2c, 0xd7,
    0x4f, 0xae, 0xd5, 0xe9, 0xe6, 0xe7, 0xad, 0xe8, 0x74, 0xd6, 0xf4, 0xea, 0xa8, 0x50, 0x58, 0xaf
];

// Galios field exponent table
gexp = [
    0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0x1d, 0x3a, 0x74, 0xe8, 0xcd, 0x87, 0x13, 0x26,
    0x4c, 0x98, 0x2d, 0x5a, 0xb4, 0x75, 0xea, 0xc9, 0x8f, 0x03, 0x06, 0x0c, 0x18, 0x30, 0x60, 0xc0,
    0x9d, 0x27, 0x4e, 0x9c, 0x25, 0x4a, 0x94, 0x35, 0x6a, 0xd4, 0xb5, 0x77, 0xee, 0xc1, 0x9f, 0x23,
    0x46, 0x8c, 0x05, 0x0a, 0x14, 0x28, 0x50, 0xa0, 0x5d, 0xba, 0x69, 0xd2, 0xb9, 0x6f, 0xde, 0xa1,
    0x5f, 0xbe, 0x61, 0xc2, 0x99, 0x2f, 0x5e, 0xbc, 0x65, 0xca, 0x89, 0x0f, 0x1e, 0x3c, 0x78, 0xf0,
    0xfd, 0xe7, 0xd3, 0xbb, 0x6b, 0xd6, 0xb1, 0x7f, 0xfe, 0xe1, 0xdf, 0xa3, 0x5b, 0xb6, 0x71, 0xe2,
    0xd9, 0xaf, 0x43, 0x86, 0x11, 0x22, 0x44, 0x88, 0x0d, 0x1a, 0x34, 0x68, 0xd0, 0xbd, 0x67, 0xce,
    0x81, 0x1f, 0x3e, 0x7c, 0xf8, 0xed, 0xc7, 0x93, 0x3b, 0x76, 0xec, 0xc5, 0x97, 0x33, 0x66, 0xcc,
    0x85, 0x17, 0x2e, 0x5c, 0xb8, 0x6d, 0xda, 0xa9, 0x4f, 0x9e, 0x21, 0x42, 0x84, 0x15, 0x2a, 0x54,
    0xa8, 0x4d, 0x9a, 0x29, 0x52, 0xa4, 0x55, 0xaa, 0x49, 0x92, 0x39, 0x72, 0xe4, 0xd5, 0xb7, 0x73,
    0xe6, 0xd1, 0xbf, 0x63, 0xc6, 0x91, 0x3f, 0x7e, 0xfc, 0xe5, 0xd7, 0xb3, 0x7b, 0xf6, 0xf1, 0xff,
    0xe3, 0xdb, 0xab, 0x4b, 0x96, 0x31, 0x62, 0xc4, 0x95, 0x37, 0x6e, 0xdc, 0xa5, 0x57, 0xae, 0x41,
    0x82, 0x19, 0x32, 0x64, 0xc8, 0x8d, 0x07, 0x0e, 0x1c, 0x38, 0x70, 0xe0, 0xdd, 0xa7, 0x53, 0xa6,
    0x51, 0xa2, 0x59, 0xb2, 0x79, 0xf2, 0xf9, 0xef, 0xc3, 0x9b, 0x2b, 0x56, 0xac, 0x45, 0x8a, 0x09,
    0x12, 0x24, 0x48, 0x90, 0x3d, 0x7a, 0xf4, 0xf5, 0xf7, 0xf3, 0xfb, 0xeb, 0xcb, 0x8b, 0x0b, 0x16,
    0x2c, 0x58, 0xb0, 0x7d, 0xfa, 0xe9, 0xcf, 0x83, 0x1b, 0x36, 0x6c, 0xd8, 0xad, 0x47, 0x8e, 0x00
];

// Working buffers:
// data input and ecc append, image working buffer, fixed part of image, run lengths for badness
var strinbuf=[], eccbuf=[], qrframe=[], framask=[], rlens=[];
// Control values - width is based on version, last 4 are from table.
var version, width, neccblk1, neccblk2, datablkw, eccblkwid;
var ecclevel = 1;
// set bit to indicate cell in qrframe is immutable.  symmetric around diagonal
function setmask(x, y)
{
    var bt;
    if (x > y) {
        bt = x;
        x = y;
        y = bt;
    }
    // y*y = 1+3+5...
    bt = y;
    bt *= y;
    bt += y;
    bt >>= 1;
    bt += x;
    framask[bt] = 1;
}

// enter alignment pattern - black to qrframe, white to mask (later black frame merged to mask)
function putalign(x, y)
{
    var j;

    qrframe[x + width * y] = 1;
    for (j = -2; j < 2; j++) {
        qrframe[(x + j) + width * (y - 2)] = 1;
        qrframe[(x - 2) + width * (y + j + 1)] = 1;
        qrframe[(x + 2) + width * (y + j)] = 1;
        qrframe[(x + j + 1) + width * (y + 2)] = 1;
    }
    for (j = 0; j < 2; j++) {
        setmask(x - 1, y + j);
        setmask(x + 1, y - j);
        setmask(x - j, y - 1);
        setmask(x + j, y + 1);
    }
}

//========================================================================
// Reed Solomon error correction
// exponentiation mod N
function modnn(x)
{
    while (x >= 255) {
        x -= 255;
        x = (x >> 8) + (x & 255);
    }
    return x;
}

var genpoly = [];

// Calculate and append ECC data to data block.  Block is in strinbuf, indexes to buffers given.
function appendrs(data, dlen, ecbuf, eclen)
{
    var i, j, fb;

    for (i = 0; i < eclen; i++)
        strinbuf[ecbuf + i] = 0;
    for (i = 0; i < dlen; i++) {
        fb = glog[strinbuf[data + i] ^ strinbuf[ecbuf]];
        if (fb != 255)     /* fb term is non-zero */
            for (j = 1; j < eclen; j++)
                strinbuf[ecbuf + j - 1] = strinbuf[ecbuf + j] ^ gexp[modnn(fb + genpoly[eclen - j])];
        else
            for( j = ecbuf ; j < ecbuf + eclen; j++ )
                strinbuf[j] = strinbuf[j + 1];
        strinbuf[ ecbuf + eclen - 1] = fb == 255 ? 0 : gexp[modnn(fb + genpoly[0])];
    }
}

//========================================================================
// Frame data insert following the path rules

// check mask - since symmetrical use half.
function ismasked(x, y)
{
    var bt;
    if (x > y) {
        bt = x;
        x = y;
        y = bt;
    }
    bt = y;
    bt += y * y;
    bt >>= 1;
    bt += x;
    return framask[bt];
}

//========================================================================
//  Apply the selected mask out of the 8.
function  applymask(m)
{
    var x, y, r3x, r3y;

    switch (m) {
    case 0:
        for (y = 0; y < width; y++)
            for (x = 0; x < width; x++)
                if (!((x + y) & 1) && !ismasked(x, y))
                    qrframe[x + y * width] ^= 1;
        break;
    case 1:
        for (y = 0; y < width; y++)
            for (x = 0; x < width; x++)
                if (!(y & 1) && !ismasked(x, y))
                    qrframe[x + y * width] ^= 1;
        break;
    case 2:
        for (y = 0; y < width; y++)
            for (r3x = 0, x = 0; x < width; x++, r3x++) {
                if (r3x == 3)
                    r3x = 0;
                if (!r3x && !ismasked(x, y))
                    qrframe[x + y * width] ^= 1;
            }
        break;
    case 3:
        for (r3y = 0, y = 0; y < width; y++, r3y++) {
            if (r3y == 3)
                r3y = 0;
            for (r3x = r3y, x = 0; x < width; x++, r3x++) {
                if (r3x == 3)
                    r3x = 0;
                if (!r3x && !ismasked(x, y))
                    qrframe[x + y * width] ^= 1;
            }
        }
        break;
    case 4:
        for (y = 0; y < width; y++)
            for (r3x = 0, r3y = ((y >> 1) & 1), x = 0; x < width; x++, r3x++) {
                if (r3x == 3) {
                    r3x = 0;
                    r3y = !r3y;
                }
                if (!r3y && !ismasked(x, y))
                    qrframe[x + y * width] ^= 1;
            }
        break;
    case 5:
        for (r3y = 0, y = 0; y < width; y++, r3y++) {
            if (r3y == 3)
                r3y = 0;
            for (r3x = 0, x = 0; x < width; x++, r3x++) {
                if (r3x == 3)
                    r3x = 0;
                if (!((x & y & 1) + !(!r3x | !r3y)) && !ismasked(x, y))
                    qrframe[x + y * width] ^= 1;
            }
        }
        break;
    case 6:
        for (r3y = 0, y = 0; y < width; y++, r3y++) {
            if (r3y == 3)
                r3y = 0;
            for (r3x = 0, x = 0; x < width; x++, r3x++) {
                if (r3x == 3)
                    r3x = 0;
                if (!(((x & y & 1) + (r3x && (r3x == r3y))) & 1) && !ismasked(x, y))
                    qrframe[x + y * width] ^= 1;
            }
        }
        break;
    case 7:
        for (r3y = 0, y = 0; y < width; y++, r3y++) {
            if (r3y == 3)
                r3y = 0;
            for (r3x = 0, x = 0; x < width; x++, r3x++) {
                if (r3x == 3)
                    r3x = 0;
                if (!(((r3x && (r3x == r3y)) + ((x + y) & 1)) & 1) && !ismasked(x, y))
                    qrframe[x + y * width] ^= 1;
            }
        }
        break;
    }
    return;
}

// Badness coefficients.
var N1 = 3, N2 = 3, N3 = 40, N4 = 10;

// Using the table of the length of each run, calculate the amount of bad image
// - long runs or those that look like finders; called twice, once each for X and Y
function badruns(length)
{
    var i;
    var runsbad = 0;
    for (i = 0; i <= length; i++)
        if (rlens[i] >= 5)
            runsbad += N1 + rlens[i] - 5;
    // BwBBBwB as in finder
    for (i = 3; i < length - 1; i += 2)
        if (rlens[i - 2] == rlens[i + 2]
            && rlens[i + 2] == rlens[i - 1]
            && rlens[i - 1] == rlens[i + 1]
            && rlens[i - 1] * 3 == rlens[i]
            // white around the black pattern? Not part of spec
            && (rlens[i - 3] == 0 // beginning
                || i + 3 > length  // end
                || rlens[i - 3] * 3 >= rlens[i] * 4 || rlens[i + 3] * 3 >= rlens[i] * 4)
           )
            runsbad += N3;
    return runsbad;
}

// Calculate how bad the masked image is - blocks, imbalance, runs, or finders.
function badcheck()
{
    var x, y, h, b, b1;
    var thisbad = 0;
    var bw = 0;

    // blocks of same color.
    for (y = 0; y < width - 1; y++)
        for (x = 0; x < width - 1; x++)
            if ((qrframe[x + width * y] && qrframe[(x + 1) + width * y]
                 && qrframe[x + width * (y + 1)] && qrframe[(x + 1) + width * (y + 1)]) // all black
                || !(qrframe[x + width * y] || qrframe[(x + 1) + width * y]
                     || qrframe[x + width * (y + 1)] || qrframe[(x + 1) + width * (y + 1)])) // all white
                thisbad += N2;

    // X runs
    for (y = 0; y < width; y++) {
        rlens[0] = 0;
        for (h = b = x = 0; x < width; x++) {
            if ((b1 = qrframe[x + width * y]) == b)
                rlens[h]++;
            else
                rlens[++h] = 1;
            b = b1;
            bw += b ? 1 : -1;
        }
        thisbad += badruns(h);
    }

    // black/white imbalance
    if (bw < 0)
        bw = -bw;

    var big = bw;
    count = 0;
    big += big << 2;
    big <<= 1;
    while (big > width * width)
        big -= width * width, count++;
    thisbad += count * N4;

    // Y runs
    for (x = 0; x < width; x++) {
        rlens[0] = 0;
        for (h = b = y = 0; y < width; y++) {
            if ((b1 = qrframe[x + width * y]) == b)
                rlens[h]++;
            else
                rlens[++h] = 1;
            b = b1;
        }
        thisbad += badruns(h);
    }
    return thisbad;
}

function genframe(instring)
{
    var x, y, k, t, v, i, j, m;

// find the smallest version that fits the string
    t = instring.length;
    version = 0;
    do {
        version++;
        k = (ecclevel - 1) * 4 + (version - 1) * 16;
        neccblk1 = eccblocks[k++];
        neccblk2 = eccblocks[k++];
        datablkw = eccblocks[k++];
        eccblkwid = eccblocks[k];
        k = datablkw * (neccblk1 + neccblk2) + neccblk2 - 3 + (version <= 9);
        if (t <= k)
            break;
    } while (version < 40);

// FIXME - insure that it fits insted of being truncated
    width = 17 + 4 * version;

// allocate, clear and setup data structures
    v = datablkw + (datablkw + eccblkwid) * (neccblk1 + neccblk2) + neccblk2;
    for( t = 0; t < v; t++ )
        eccbuf[t] = 0;
    strinbuf = instring.slice(0);

    for( t = 0; t < width * width; t++ )
        qrframe[t] = 0;

    for( t = 0 ; t < (width * (width + 1) + 1) / 2; t++)
        framask[t] = 0;

// insert finders - black to frame, white to mask
    for (t = 0; t < 3; t++) {
        k = 0;
        y = 0;
        if (t == 1)
            k = (width - 7);
        if (t == 2)
            y = (width - 7);
        qrframe[(y + 3) + width * (k + 3)] = 1;
        for (x = 0; x < 6; x++) {
            qrframe[(y + x) + width * k] = 1;
            qrframe[y + width * (k + x + 1)] = 1;
            qrframe[(y + 6) + width * (k + x)] = 1;
            qrframe[(y + x + 1) + width * (k + 6)] = 1;
        }
        for (x = 1; x < 5; x++) {
            setmask(y + x, k + 1);
            setmask(y + 1, k + x + 1);
            setmask(y + 5, k + x);
            setmask(y + x + 1, k + 5);
        }
        for (x = 2; x < 4; x++) {
            qrframe[(y + x) + width * (k + 2)] = 1;
            qrframe[(y + 2) + width * (k + x + 1)] = 1;
            qrframe[(y + 4) + width * (k + x)] = 1;
            qrframe[(y + x + 1) + width * (k + 4)] = 1;
        }
    }

// alignment blocks
    if (version > 1) {
        t = adelta[version];
        y = width - 7;
        for (;;) {
            x = width - 7;
            while (x > t - 3) {
                putalign(x, y);
                if (x < t)
                    break;
                x -= t;
            }
            if (y <= t + 9)
                break;
            y -= t;
            putalign(6, y);
            putalign(y, 6);
        }
    }

// single black
    qrframe[8 + width * (width - 8)] = 1;

// timing gap - mask only
    for (y = 0; y < 7; y++) {
        setmask(7, y);
        setmask(width - 8, y);
        setmask(7, y + width - 7);
    }
    for (x = 0; x < 8; x++) {
        setmask(x, 7);
        setmask(x + width - 8, 7);
        setmask(x, width - 8);
    }

// reserve mask-format area
    for (x = 0; x < 9; x++)
        setmask(x, 8);
    for (x = 0; x < 8; x++) {
        setmask(x + width - 8, 8);
        setmask(8, x);
    }
    for (y = 0; y < 7; y++)
        setmask(8, y + width - 7);

// timing row/col
    for (x = 0; x < width - 14; x++)
        if (x & 1) {
            setmask(8 + x, 6);
            setmask(6, 8 + x);
        }
        else {
            qrframe[(8 + x) + width * 6] = 1;
            qrframe[6 + width * (8 + x)] = 1;
        }

// version block
    if (version > 6) {
        t = vpat[version - 7];
        k = 17;
        for (x = 0; x < 6; x++)
            for (y = 0; y < 3; y++, k--)
                if (1 & (k > 11 ? version >> (k - 12) : t >> k)) {
                    qrframe[(5 - x) + width * (2 - y + width - 11)] = 1;
                    qrframe[(2 - y + width - 11) + width * (5 - x)] = 1;
                }
        else {
            setmask(5 - x, 2 - y + width - 11);
            setmask(2 - y + width - 11, 5 - x);
        }
    }

// sync mask bits - only set above for white spaces, so add in black bits
    for (y = 0; y < width; y++)
        for (x = 0; x <= y; x++)
            if (qrframe[x + width * y])
                setmask(x, y);

// convert string to bitstream
// 8 bit data to QR-coded 8 bit data (numeric or alphanum, or kanji not supported)
    v = strinbuf.length;

// string to array
    for( i = 0 ; i < v; i++ )
        eccbuf[i] = strinbuf.charCodeAt(i);
    strinbuf = eccbuf.slice(0);

// calculate max string length
    x = datablkw * (neccblk1 + neccblk2) + neccblk2;
    if (v >= x - 2) {
        v = x - 2;
        if (version > 9)
            v--;
    }

// shift and repack to insert length prefix
    i = v;
    if (version > 9) {
        strinbuf[i + 2] = 0;
        strinbuf[i + 3] = 0;
        while (i--) {
            t = strinbuf[i];
            strinbuf[i + 3] |= 255 & (t << 4);
            strinbuf[i + 2] = t >> 4;
        }
        strinbuf[2] |= 255 & (v << 4);
        strinbuf[1] = v >> 4;
        strinbuf[0] = 0x40 | (v >> 12);
    }
    else {
        strinbuf[i + 1] = 0;
        strinbuf[i + 2] = 0;
        while (i--) {
            t = strinbuf[i];
            strinbuf[i + 2] |= 255 & (t << 4);
            strinbuf[i + 1] = t >> 4;
        }
        strinbuf[1] |= 255 & (v << 4);
        strinbuf[0] = 0x40 | (v >> 4);
    }
// fill to end with pad pattern
    i = v + 3 - (version < 10);
    while (i < x) {
        strinbuf[i++] = 0xec;
        // buffer has room    if (i == x)      break;
        strinbuf[i++] = 0x11;
    }

// calculate and append ECC

// calculate generator polynomial
    genpoly[0] = 1;
    for (i = 0; i < eccblkwid; i++) {
        genpoly[i + 1] = 1;
        for (j = i; j > 0; j--)
            genpoly[j] = genpoly[j]
            ? genpoly[j - 1] ^ gexp[modnn(glog[genpoly[j]] + i)] : genpoly[j - 1];
        genpoly[0] = gexp[modnn(glog[genpoly[0]] + i)];
    }
    for (i = 0; i <= eccblkwid; i++)
        genpoly[i] = glog[genpoly[i]]; // use logs for genpoly[] to save calc step

// append ecc to data buffer
    k = x;
    y = 0;
    for (i = 0; i < neccblk1; i++) {
        appendrs(y, datablkw, k, eccblkwid);
        y += datablkw;
        k += eccblkwid;
    }
    for (i = 0; i < neccblk2; i++) {
        appendrs(y, datablkw + 1, k, eccblkwid);
        y += datablkw + 1;
        k += eccblkwid;
    }
// interleave blocks
    y = 0;
    for (i = 0; i < datablkw; i++) {
        for (j = 0; j < neccblk1; j++)
            eccbuf[y++] = strinbuf[i + j * datablkw];
        for (j = 0; j < neccblk2; j++)
            eccbuf[y++] = strinbuf[(neccblk1 * datablkw) + i + (j * (datablkw + 1))];
    }
    for (j = 0; j < neccblk2; j++)
        eccbuf[y++] = strinbuf[(neccblk1 * datablkw) + i + (j * (datablkw + 1))];
    for (i = 0; i < eccblkwid; i++)
        for (j = 0; j < neccblk1 + neccblk2; j++)
            eccbuf[y++] = strinbuf[x + i + j * eccblkwid];
    strinbuf = eccbuf;

// pack bits into frame avoiding masked area.
    x = y = width - 1;
    k = v = 1;         // up, minus
    /* inteleaved data and ecc codes */
    m = (datablkw + eccblkwid) * (neccblk1 + neccblk2) + neccblk2;
    for (i = 0; i < m; i++) {
        t = strinbuf[i];
        for (j = 0; j < 8; j++, t <<= 1) {
            if (0x80 & t)
                qrframe[x + width * y] = 1;
            do {        // find next fill position
                if (v)
                    x--;
                else {
                    x++;
                    if (k) {
                        if (y != 0)
                            y--;
                        else {
                            x -= 2;
                            k = !k;
                            if (x == 6) {
                                x--;
                                y = 9;
                            }
                        }
                    }
                    else {
                        if (y != width - 1)
                            y++;
                        else {
                            x -= 2;
                            k = !k;
                            if (x == 6) {
                                x--;
                                y -= 8;
                            }
                        }
                    }
                }
                v = !v;
            } while (ismasked(x, y));
        }
    }

// save pre-mask copy of frame
    strinbuf = qrframe.slice(0);
    t = 0;           // best
    y = 30000;         // demerit
// for instead of while since in original arduino code
// if an early mask was "good enough" it wouldn't try for a better one
// since they get more complex and take longer.
    for (k = 0; k < 8; k++) {
        applymask(k);      // returns black-white imbalance
        x = badcheck();
        if (x < y) { // current mask better than previous best?
            y = x;
            t = k;
        }
        if (t == 7)
            break;       // don't increment i to a void redoing mask
        qrframe = strinbuf.slice(0); // reset for next pass
    }
    if (t != k)         // redo best mask - none good enough, last wasn't t
        applymask(t);

// add in final mask/ecclevel bytes
    y = fmtword[t + ((ecclevel - 1) << 3)];
    // low byte
    for (k = 0; k < 8; k++, y >>= 1)
        if (y & 1) {
            qrframe[(width - 1 - k) + width * 8] = 1;
            if (k < 6)
                qrframe[8 + width * k] = 1;
            else
                qrframe[8 + width * (k + 1)] = 1;
        }
    // high byte
    for (k = 0; k < 7; k++, y >>= 1)
        if (y & 1) {
            qrframe[8 + width * (width - 7 + k)] = 1;
            if (k)
                qrframe[(6 - k) + width * 8] = 1;
            else
                qrframe[7 + width * 8] = 1;
        }

// return image
    return qrframe;
}

function toggleFunction() {
    var togglediv = document.getElementById('qrcanvdiv');
    if (togglediv.style.display === 'none') {
        togglediv.style.display = 'block';
        document.getElementById("scanQR").innerHTML = "<span class=\"glyphicon glyphicon-collapse-up\"></span>";
    } else {
        togglediv.style.display = 'none';
        document.getElementById("scanQR").innerHTML = "<span class=\"glyphicon glyphicon-collapse-down\"></span>";
    }
}

var wd, ht, qrc;
function setupqr(){
//    window.scrollTo(0,1)
    wd = window.innerWidth*0.25-10;
    ht = window.innerHeight*0.75-10;
    mp = document.getElementById("mapcanv");
    qrd = document.getElementById("qrdiv");
    qrd.style.width = wd + "px";
    qrd.style.height = ht/2 + "px";

    wd -= 4;
    ht = 3*ht/4;
    var elem = document.getElementById('qrcanv');
    qrc = elem.getContext('2d');
    qrc.canvas.width = wd;
    qrc.canvas.height = ht;
    qrc.fillStyle = '#eee';
    qrc.fillRect(0,0,wd,ht);

}

function doqr(key) {
    d = document;
    ecclevel = d.qrinp.ECC.value;
    qf = genframe(key);
    qrc.lineWidth=1;

    var i,j;
    px = wd;
    if( ht < wd )
        px = ht;
    px /= width+10;
    px=Math.round(px - 0.5);
    qrc.clearRect(0,0,wd,ht);
    qrc.fillStyle = '#fff';
    qrc.fillRect(0,0,px*(width+8),px*(width+8));
    qrc.fillStyle = '#000';
    for( i = 0; i < width; i++ )
        for( j = 0; j < width; j++ )
            if( qf[j*width+i] )
                qrc.fillRect(px*(4+i),px*(4+j),px,px)
}
