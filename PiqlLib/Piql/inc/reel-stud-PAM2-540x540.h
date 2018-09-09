// *****************************************************************************
// ** This file is generated automatically by convertxmltocsource application **
// *****************************************************************************

#if !defined(CONFIG_SOURCE_DEFINITIONS)
#define CONFIG_SOURCE_DEFINITIONS
#define CONFIG_ARRAY_SIZE(a)   sizeof(a) / sizeof(a[0])

typedef struct config_pair_s
{
    const char* key;
    const char* value;
}config_pair;

typedef struct config_class_s
{
    const char* name;
    const char* version;
    config_pair * pairs;
    unsigned int count;
}config_class;

typedef struct config_structure_s
{
    const char * version;
    config_class * classes;
    const unsigned int count;
}config_structure;

#endif // CONFIG_SOURCE_DEFINITIONS

config_pair pairsFormatInfo_540x540_1[] =
{
    { "name", "4k-stud-PAM2-540x540" },
    { "shortDescription", "studend project - PAM4 modulation @ 540x540 raster" },
    { "description", "Frames are generated using the Generic Preservation Format v1.1 printed on a 4096x2160 raster" }
};

config_pair pairsFrameFormat_540x540_2[] =
{
    { "type", "GPFv1.1" },
    { "width", "540" },
    { "height", "540" },
    { "border", "1" },
    { "borderGap", "1" },
    { "cornerMarkSize", "32" },
    { "cornerMarkGap", "1" },
    { "tilesPerColumn", "5" },
    { "refBarSyncDistance", "58" },
    { "refBarSyncOffset", "30" },
    /*  color dept = log2(maxLevelsPerSymbol) */
    { "maxLevelsPerSymbol", "2" }
};

config_pair pairsFrameRaster_540x540_3[] =
{
    { "width", "540" },
    { "height", "540" },
    { "scalingFactor", "1" }
};

config_pair pairsCodecDispatcher_540x540_4[] =
{
    /*  Frame coding scheme */
    { "version", "1.0" },
    /*  The order the encoder process is specified, default value: encode */
    { "order", "decode" },
    /*  symbol alignment default: bit */
    { "symbolAlignment", "byte" },
    { "DataCodingScheme", "SyncPointInserter,Modulator,Cipher,FInterleave,RS_inner,XInterleave,RS_outer,CRC,PacketHeader" },
    { "MetadataCodingScheme", "MetaData_Modulator,MetaData_Cipher,MetaData_ReedSolomon,MetaData_CRC,PacketHeader" }
};

config_pair pairsPacketHeader_540x540_5[] =
{
    { "codec", "PacketHeader" }
};

config_pair pairsCRC_540x540_6[] =
{
    { "codec", "CRC64" },
    { "polynom", "0x42F0E1EBA9EA3693" },
    { "seed", "0x0000000000000000" }
};

config_pair pairsRS_outer_540x540_7[] =
{
    { "codec", "ReedSolomon" },
    { "byteParityNumber", "20" },
    { "messageSize", "172" }
};

config_pair pairsXInterleave_540x540_8[] =
{
    { "codec", "Interleaving" },
    { "distance", "192" },
    /*  interleaving symboltype; block, frame */
    { "interleavingtype", "block" },
    /*  interleaving symboltype; byte, bit */
    { "symboltype", "byte" }
};

config_pair pairsRS_inner_540x540_9[] =
{
    { "codec", "ReedSolomon" },
    { "byteParityNumber", "16" },
    { "messageSize", "192" }
};

config_pair pairsFInterleave_540x540_10[] =
{
    { "codec", "Interleaving" },
    { "distance", "239" },
    { "interleavingtype", "block" },
    /*  interleaving symboltype; byte, bit */
    { "symboltype", "byte" }
};

config_pair pairsCipher_540x540_11[] =
{
    { "codec", "Cipher" },
    /*  chipher key ; 'auto' , 'any number supported by the chiper class' */
    { "key", "auto" }
};

config_pair pairsModulator_540x540_12[] =
{
    { "codec", "Modulator" },
    /*  NumBitsPerPixel; auto - any supported color depth */
    { "NumBitsPerPixel", "auto" }
};

config_pair pairsSyncPointInserter_540x540_13[] =
{
    { "codec", "SyncPointInserter" },
    { "SyncPointDistancePixel", "58" },
    { "SyncPointVOffsetPixel", "30" },
    { "SyncPointHOffsetPixel", "30" },
    { "SyncPointRadiusPixel", "2" },
    { "DataOrientation", "1" },
    /*  NumBitsPerPixel; auto - any supported color depth */
    { "NumBitsPerPixel", "auto" }
};

config_pair pairsMetaData_CRC_540x540_14[] =
{
    { "codec", "CRC32" },
    { "polynom", "0x1EDC6F41" },
    { "seed", "0x00000000" }
};

config_pair pairsMetaData_ReedSolomon_540x540_15[] =
{
    { "codec", "ReedSolomon" },
    { "byteParityNumber", "20" },
    { "messageSize", "58" }
};

config_pair pairsMetaData_Modulator_540x540_16[] =
{
    { "codec", "Modulator" },
    /*  NumBitsPerPixel; auto - any supported color depth */
    { "NumBitsPerPixel", "auto" }
};

config_pair pairsMetaData_Cipher_540x540_17[] =
{
    { "codec", "Cipher" },
    /*  chipher key ; 'auto' , 'any number supported by the chiper class' */
    { "key", "1" }
};

config_class config_classes_540x540[] =
{
    { "FormatInfo", NULL, pairsFormatInfo_540x540_1, CONFIG_ARRAY_SIZE(pairsFormatInfo_540x540_1) },
    { "FrameFormat", NULL, pairsFrameFormat_540x540_2, CONFIG_ARRAY_SIZE(pairsFrameFormat_540x540_2) },
    { "FrameRaster", NULL, pairsFrameRaster_540x540_3, CONFIG_ARRAY_SIZE(pairsFrameRaster_540x540_3) },
    { "CodecDispatcher", NULL, pairsCodecDispatcher_540x540_4, CONFIG_ARRAY_SIZE(pairsCodecDispatcher_540x540_4) },
    { "PacketHeader", NULL, pairsPacketHeader_540x540_5, CONFIG_ARRAY_SIZE(pairsPacketHeader_540x540_5) },
    { "CRC", NULL, pairsCRC_540x540_6, CONFIG_ARRAY_SIZE(pairsCRC_540x540_6) },
    { "RS_outer", NULL, pairsRS_outer_540x540_7, CONFIG_ARRAY_SIZE(pairsRS_outer_540x540_7) },
    { "XInterleave", NULL, pairsXInterleave_540x540_8, CONFIG_ARRAY_SIZE(pairsXInterleave_540x540_8) },
    { "RS_inner", NULL, pairsRS_inner_540x540_9, CONFIG_ARRAY_SIZE(pairsRS_inner_540x540_9) },
    { "FInterleave", NULL, pairsFInterleave_540x540_10, CONFIG_ARRAY_SIZE(pairsFInterleave_540x540_10) },
    { "Cipher", NULL, pairsCipher_540x540_11, CONFIG_ARRAY_SIZE(pairsCipher_540x540_11) },
    { "Modulator", NULL, pairsModulator_540x540_12, CONFIG_ARRAY_SIZE(pairsModulator_540x540_12) },
    { "SyncPointInserter", NULL, pairsSyncPointInserter_540x540_13, CONFIG_ARRAY_SIZE(pairsSyncPointInserter_540x540_13) },
    { "MetaData_CRC", NULL, pairsMetaData_CRC_540x540_14, CONFIG_ARRAY_SIZE(pairsMetaData_CRC_540x540_14) },
    { "MetaData_ReedSolomon", NULL, pairsMetaData_ReedSolomon_540x540_15, CONFIG_ARRAY_SIZE(pairsMetaData_ReedSolomon_540x540_15) },
    { "MetaData_Modulator", NULL, pairsMetaData_Modulator_540x540_16, CONFIG_ARRAY_SIZE(pairsMetaData_Modulator_540x540_16) },
    { "MetaData_Cipher", NULL, pairsMetaData_Cipher_540x540_17, CONFIG_ARRAY_SIZE(pairsMetaData_Cipher_540x540_17) }};

config_structure config_source_540x540 = { "1.0.0.0", config_classes_540x540, CONFIG_ARRAY_SIZE(config_classes_540x540) };
