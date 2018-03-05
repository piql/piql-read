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
TARGET                      =   make-gen
CONFIG                     +=   console
win32:CONFIG               +=   qt 
TEMPLATE                    =   app
DESTDIR                     =   $$PLATFORM/$$CURBUILD/

INCLUDEPATH                +=   $$INCLUDE_UNBOXING_LIB() \
                                $$PWD/../boxingdata/inc \
                                $$PWD/inc
								
LIBS                       +=   $$LINK_UNBOXING_LIB()


##############################################################################

##  HEADER FILES  ##

HEADERS += 

##  SOURCE FILES  ##

SOURCES += make-gen.c
