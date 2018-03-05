INCLUDEPATH += $$PWD

DEFINES += RAND_FILE=\\\"./randfile\\\"

HEADERS += \
    $$PWD/alloc.h \
    $$PWD/blockio.h \
    $$PWD/channel.h \
    $$PWD/check.h \
    $$PWD/dec.h \
    $$PWD/distrib.h \
    $$PWD/enc.h \
    $$PWD/intio.h \
    $$PWD/mod2convert.h \
    $$PWD/mod2dense.h \
    $$PWD/mod2sparse.h \
    $$PWD/open.h \
    $$PWD/rand.h \
    $$PWD/rcode.h

SOURCES += \
    $$PWD/alloc.c \
    $$PWD/blockio.c \
    $$PWD/channel.c \
    $$PWD/check.c \
    $$PWD/dec.c \
    $$PWD/distrib.c \
    $$PWD/enc.c \
    $$PWD/intio.c \
    $$PWD/mod2convert.c \
    $$PWD/mod2dense.c \
    $$PWD/mod2sparse.c \
    $$PWD/open.c \
    $$PWD/rand.c \
    $$PWD/rcode.c \
    $$PWD/verify.c