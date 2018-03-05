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
SUBDIRS               =   encode decode make-gen make-ldpc rand-src transmit verify extract

encode.file       = encode.pro
decode.file       = decode.pro
make-gen.file     = make-gen.pro
make-ldpc.file    = make-ldpc.pro
rand-src.file     = rand-src.pro
transmit.file     = transmit.pro
verify.file       = verify.pro
extract.file      = extract.pro
