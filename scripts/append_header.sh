#!/bin/bash

INFILE=$1
if [ "$INFILE" == "" ] ; then
    echo "usage : " \
    "$0 <infile> [outfile]"
    exit 1
fi

OUTFILE=$2
if [ "$OUTFILE" == "" ] ; then    
    OUTFILE=$1.bin
fi

if [ "$OUTFILE" == "$INFILE" ] ; then
    echo "source and destination is the same file"
    exit 1
fi

echo "$INFILE >> $OUTFILE"

## build header
HEADER=$(mktemp)
echo -n $INFILE > $HEADER;
dd if=/dev/zero bs=64 count=1 2>/dev/random >> $HEADER;

## Save header to destinaiton file file
dd if=$HEADER bs=64 count=1 2>/dev/random > $OUTFILE

## Append source file to destination file
dd if=$1 2>/dev/random >> $OUTFILE

## cleanup
rm $HEADER
