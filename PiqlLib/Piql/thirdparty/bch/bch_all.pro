##############################################################################
#
#   Creation date:  2013-07-01
#   Created by:     Matti Wennberg
#
#   Copyright (c) 2013 Piql AS, Norway. All rights reserved.
#
##############################################################################


##  CONFIG
TEMPLATE               = subdirs  
CONFIG                +=   ordered


##  FILES
SUBDIRS               =   bch bch_test


bch_test.file       = bch_test.pro
bch_test.depends    = bch

## encode.file       = encode.pro
## encode.depends    = bch
## 
## decode.file       = decode.pro
## decode.depends    = bch

bch.file          = bch.pro
