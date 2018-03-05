##############################################################################
#
#   Creation date:  2013-05-14
#   Created by:     Ole Liabo
#
#   Copyright (c) 2013 Piql AS, Norway. All rights reserved.
#
##############################################################################

!include (../../../boxinglib.pri) {
    error( "boxinglib.pri not found" )
}

##  GENERAL  ##
TARGET                      =   bch_test
CONFIG                     +=   console
win32:CONFIG               +=   qt 
TEMPLATE                    =   app
DESTDIR                     =   $$PLATFORM/$$CURBUILD/

win32:LIBS                 +=   $$(CV_BUILD_ROOT)/$$PLATFORM/$$CURBUILD/lib/bch.lib
unix:LIBS                  +=   $$(CV_BUILD_ROOT)/$$PLATFORM/$$CURBUILD/lib/libbch.a

##############################################################################

##  HEADER FILES  ##

HEADERS += 

##  SOURCE FILES  ##

SOURCES += bch_test.c
