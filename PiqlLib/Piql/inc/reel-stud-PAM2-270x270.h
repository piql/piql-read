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

config_pair pairsFormatInfo_270x270_1[] =
{
    { "name", "4k-stud-PAM2-270x270" },
    { "shortDescription", "studend project - PAM4 modulation @ 270x270 raster" },
    { "description", "Frames are generated using the Generic Preservation Format v1.1 printed on a 4096x2160 raster" }
};

config_pair pairsFrameFormat_270x270_2[] =
{
    { "type", "GPFv1.1" },
    { "width", "270" },
    { "height", "270" },
    { "border", "1" },
    { "borderGap", "1" },
    { "cornerMarkSize", "32" },
    { "cornerMarkGap", "1" },
    { "tilesPerColumn", "8" },
    { "refBarSyncDistance", "50" },
    { "refBarSyncOffset", "26" },
    /*  color dept = log2(maxLevelsPerSymbol) */
    { "maxLevelsPerSymbol", "2" }
};

config_pair pairsFrameRaster_270x270_3[] =
{
    { "width", "270" },
    { "height", "270" },
    { "scalingFactor", "1" }
};

config_pair pairsCodecDispatcher_270x270_4[] =
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

config_pair pairsPacketHeader_270x270_5[] =
{
    { "codec", "PacketHeader" }
};

config_pair pairsCRC_270x270_6[] =
{
    { "codec", "CRC64" },
    { "polynom", "0x42F0E1EBA9EA3693" },
    { "seed", "0x0000000000000000" }
};

config_pair pairsRS_outer_270x270_7[] =
{
    { "codec", "ReedSolomon" },
    { "byteParityNumber", "20" },
    { "messageSize", "172" }
};

config_pair pairsXInterleave_270x270_8[] =
{
    { "codec", "Interleaving" },
    { "distance", "192" },
    /*  interleaving symboltype; block, frame */
    { "interleavingtype", "block" },
    /*  interleaving symboltype; byte, bit */
    { "symboltype", "byte" }
};

config_pair pairsRS_inner_270x270_9[] =
{
    { "codec", "ReedSolomon" },
    { "byteParityNumber", "16" },
    { "messageSize", "192" }
};

config_pair pairsFInterleave_270x270_10[] =
{
    { "codec", "Interleaving" },
    { "distance", "239" },
    { "interleavingtype", "block" },
    /*  interleaving symboltype; byte, bit */
    { "symboltype", "byte" }
};

config_pair pairsCipher_270x270_11[] =
{
    { "codec", "Cipher" },
    /*  chipher key ; 'auto' , 'any number supported by the chiper class' */
    { "key", "auto" }
};

config_pair pairsModulator_270x270_12[] =
{
    { "codec", "Modulator" },
    /*  NumBitsPerPixel; auto - any supported color depth */
    { "NumBitsPerPixel", "auto" }
};

config_pair pairsSyncPointInserter_270x270_13[] =
{
    { "codec", "SyncPointInserter" },
    { "SyncPointDistancePixel", "50" },
    { "SyncPointRadiusPixel", "2" },
    { "DataOrientation", "1" },
    /*  NumBitsPerPixel; auto - any supported color depth */
    { "NumBitsPerPixel", "auto" }
};

config_pair pairsMetaData_CRC_270x270_14[] =
{
    { "codec", "CRC32" },
    { "polynom", "0x1EDC6F41" },
    { "seed", "0x00000000" }
};

config_pair pairsMetaData_ReedSolomon_270x270_15[] =
{
    { "codec", "ReedSolomon" },
    { "byteParityNumber", "32" },
    { "messageSize", "69" }
};

config_pair pairsMetaData_Modulator_270x270_16[] =
{
    { "codec", "Modulator" },
    /*  NumBitsPerPixel; auto - any supported color depth */
    { "NumBitsPerPixel", "auto" }
};

config_pair pairsMetaData_Cipher_270x270_17[] =
{
    { "codec", "Cipher" },
    /*  chipher key ; 'auto' , 'any number supported by the chiper class' */
    { "key", "1" }
};

config_class config_classes_270x270[] =
{
    { "FormatInfo", NULL, pairsFormatInfo_270x270_1, CONFIG_ARRAY_SIZE(pairsFormatInfo_270x270_1) },
    { "FrameFormat", NULL, pairsFrameFormat_270x270_2, CONFIG_ARRAY_SIZE(pairsFrameFormat_270x270_2) },
    { "FrameRaster", NULL, pairsFrameRaster_270x270_3, CONFIG_ARRAY_SIZE(pairsFrameRaster_270x270_3) },
    { "CodecDispatcher", NULL, pairsCodecDispatcher_270x270_4, CONFIG_ARRAY_SIZE(pairsCodecDispatcher_270x270_4) },
    { "PacketHeader", NULL, pairsPacketHeader_270x270_5, CONFIG_ARRAY_SIZE(pairsPacketHeader_270x270_5) },
    { "CRC", NULL, pairsCRC_270x270_6, CONFIG_ARRAY_SIZE(pairsCRC_270x270_6) },
    { "RS_outer", NULL, pairsRS_outer_270x270_7, CONFIG_ARRAY_SIZE(pairsRS_outer_270x270_7) },
    { "XInterleave", NULL, pairsXInterleave_270x270_8, CONFIG_ARRAY_SIZE(pairsXInterleave_270x270_8) },
    { "RS_inner", NULL, pairsRS_inner_270x270_9, CONFIG_ARRAY_SIZE(pairsRS_inner_270x270_9) },
    { "FInterleave", NULL, pairsFInterleave_270x270_10, CONFIG_ARRAY_SIZE(pairsFInterleave_270x270_10) },
    { "Cipher", NULL, pairsCipher_270x270_11, CONFIG_ARRAY_SIZE(pairsCipher_270x270_11) },
    { "Modulator", NULL, pairsModulator_270x270_12, CONFIG_ARRAY_SIZE(pairsModulator_270x270_12) },
    { "SyncPointInserter", NULL, pairsSyncPointInserter_270x270_13, CONFIG_ARRAY_SIZE(pairsSyncPointInserter_270x270_13) },
    { "MetaData_CRC", NULL, pairsMetaData_CRC_270x270_14, CONFIG_ARRAY_SIZE(pairsMetaData_CRC_270x270_14) },
    { "MetaData_ReedSolomon", NULL, pairsMetaData_ReedSolomon_270x270_15, CONFIG_ARRAY_SIZE(pairsMetaData_ReedSolomon_270x270_15) },
    { "MetaData_Modulator", NULL, pairsMetaData_Modulator_270x270_16, CONFIG_ARRAY_SIZE(pairsMetaData_Modulator_270x270_16) },
    { "MetaData_Cipher", NULL, pairsMetaData_Cipher_270x270_17, CONFIG_ARRAY_SIZE(pairsMetaData_Cipher_270x270_17) }};

config_structure config_source_270x270 = { "1.0.0.0", config_classes_270x270, CONFIG_ARRAY_SIZE(config_classes_270x270) };
