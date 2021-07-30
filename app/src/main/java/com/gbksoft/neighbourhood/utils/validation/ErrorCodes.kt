package com.gbksoft.neighbourhood.utils.validation

object ErrorCodes {
    //===== GLOBAL ERRORS ==========================================================================
    // Front End + Back End Validations
    const val EMAIL = 1010
    const val DATE = 1020
    const val DATE_TOO_SMALL = 1021
    const val DATE_TOO_BIG = 1022
    const val FILE = 1030
    const val FILE_UPLOAD_REQUIRED = 1031
    const val FILE_TOO_MANY = 1032
    const val FILE_TOO_FEW = 1033
    const val FILE_WRONG_EXTENSION = 1034
    const val FILE_TOO_BIG = 1035
    const val FILE_TOO_SMALL = 1036
    const val FILE_WRONG_MIME_TYPE = 1037
    const val IMAGE = 1040
    const val IMAGE_UNDER_WIDTH = 1041
    const val IMAGE_UNDER_HEIGHT = 1042
    const val IMAGE_OVER_WIDTH = 1043
    const val IMAGE_OVER_HEIGHT = 1044
    const val NUMBER = 1050
    const val NUMBER_INTEGER_ONLY = 1051
    const val NUMBER_TOO_SMALL = 1052
    const val NUMBER_TOO_BIG = 1053
    const val REQUIRED = 1060
    const val REQUIRED_REQUIRED_VALUE = 1061
    const val REGULAR_EXPRESSION = 1070
    const val STRING_TOO_SHORT = 1081
    const val STRING_TOO_LONG = 1082
    const val STRING_NOT_EQUAL = 1083
    const val URL = 1090
    const val BOOLEAN = 1100
    const val COMPARE_EQUAL = 1110
    const val COMPARE_NOT_EQUAL = 1111
    const val COMPARE_GREATER_THEN = 1112
    const val COMPARE_GREATER_OR_EQUAL = 1113
    const val COMPARE_LESS_THEN = 1114
    const val COMPARE_LESS_OR_EQUAL = 1115
    const val IN = 1120
    const val IP_VALIDATOR = 1130
    const val IP_VALIDATOR_IPV6_NOT_ALLOWED = 1131
    const val IP_VALIDATOR_IPV4_NOT_ALLOWED = 1132
    const val IP_VALIDATOR_WRONG_CIDR = 1133
    const val IP_VALIDATOR_NO_SUBNET = 1134
    const val IP_VALIDATOR_HAS_SUBNET = 1135
    const val IP_VALIDATOR_NOT_IN_RANGE = 1136
    const val CAPTCHA = 1140
    const val PASSWORD_FORMAT = 1300
    const val TOO_MANY_CATEGORIES = 1302
    const val OBJECT_DURATION_TOO_LONG = 2031

    // BackEnd Validations
    const val UNIQUE = 1150
    const val UNIQUE_COMBO_NOT_UNIQUE = 1151
    const val EXIST = 1160
    const val STRING = 1180

    //===== CUSTOM ERRORS ==========================================================================
    // BackEnd Validations
    const val CREDENTIALS = 1200
    const val BLOCKED = 1210
    const val PASSWORD = 1220
    const val SAME_CURRENT_AND_NEW_PASSWORD = 1230
    const val EMAIL_NOT_VERIFIED = 1250
    const val USER_NOT_EXIST = 1260
    const val VALID_LINK = 1261
    const val EMAIL_VERIFIED = 1262
    const val IMAGE_SINGLE_SELECT = -1 // TODO: Need error code
    const val INTEREST_EXISTS = 1301
    const val USER_BLOCKED = 1345

    // BackEnd Validations
    const val CUSTOM = 3000
    const val ADDRESS_CORRECT = 1270
    const val E_3101 = 3101 // TODO: Need error name
    const val E_3102 = 3102 // TODO: Need error name
    const val E_3103 = 3103 // TODO: Need error name
    const val E_3104 = 3104 // TODO: Need error name
    const val E_3203 = 3203 // TODO: Need error name
    const val E_3303 = 3303 // TODO: Need error name
    const val E_3403 = 3403 // TODO: Need error name
}