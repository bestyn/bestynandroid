package com.gbksoft.neighbourhood.utils

object Constants {
    // Global
    /*//test
    public static final int PER_PAGE = 5;
    public static final int POST_LIST_PAGINATION_BUFFER = 1;
    public static final int POST_CHAT_PAGINATION_BUFFER = 1;*/
    const val PER_PAGE = 20
    const val NEWS_PER_PAGE = 10
    const val POST_LIST_PAGINATION_BUFFER = 5
    const val POST_CHAT_PAGINATION_BUFFER = 5
    const val NEWS_LIST_PAGINATION_BUFFER = 5
    const val PRIVATE_CHAT_PAGINATION_BUFFER = 5
    const val PROFILE_SEARCH_LIST_PAGINATION_BUFFER = 10
    const val SEARCH_COUNT_SYMBOL = 1
    const val PICTURE_FILE_MAX_LENGTH = 5 * 1024 * 1024
    const val PICTURE_PIXELS_MAX_SIZE = 1920
    const val VIDEO_FILE_MAX_LENGTH = 350 * 1024 * 1024
    const val AUDIO_FILE_MAX_LENGTH = 20 * 1024 * 1024
    const val ATTACHMENT_FILE_MAX_LENGTH = 350 * 1024 * 1024
    const val POST_MESSAGE_MAX_LENGTH = 1000
    const val PRIVATE_MESSAGE_MAX_LENGTH = 10000
    const val CHAT_LIST_PAGINATION_BUFFER = 5
    const val CHAT_SEARCH_QUERY_MIN_LENGTH = 2
    const val PIC_CROP_WIDTH_RATIO = 255
    const val PIC_CROP_HEIGHT_RATIO = 190
    const val PIC_CROP_MIN_WIDTH = PIC_CROP_WIDTH_RATIO
    const val PIC_CROP_MIN_HEIGHT = PIC_CROP_HEIGHT_RATIO
    const val RESEND_TOKEN_TIMEOUT = 120_000

    // Keys
    const val KEY_ROUTE = "key_route"
    const val KEY_BUNDLE = "key_bundle"
    const val KEY_POST_TYPE = "key_post_type"
    const val KEY_POST_ID = "key_post_id"
    const val KEY_RESET_PASSWORD_TOKEN = "resetToken"
    const val KEY_CHANGED_EMAIL = "changed_email"

    // Validation
    const val MIN_PASSWORD_LENGTH = 8
    const val MAX_PASSWORD_LENGTH = 255
    const val REGEX_PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=\\S+$).{$MIN_PASSWORD_LENGTH,}$"
    const val REGEX_NUMBER = "^-?\\d*(\\,\\d+|\\.\\d+)?$"
    const val FULL_NAME_MIN_LENGTH = 2
    const val FULL_NAME_MAX_LENGTH = 50
    const val POST_DESCRIPTION_MIN_LENGTH = 2
    const val POST_DESCRIPTION_MAX_LENGTH = 10000
    const val POST_NAME_MIN_LENGTH = 2
    const val POST_NAME_MAX_LENGTH = 100
    const val BUSINESS_NAME_MIN_LENGTH = 2
    const val BUSINESS_NAME_MAX_LENGTH = 50
    const val BUSINESS_DESCRIPTION_MIN_LENGTH = 10
    const val BUSINESS_DESCRIPTION_MAX_LENGTH = 200
    const val BUSINESS_CATEGORY_MIN_COUNT = 1
    const val BUSINESS_CATEGORY_MAX_COUNT = 5
    const val BUSINESS_PHONE_LENGTH = 10
    const val VIDEO_MAX_COUNT = 1
    const val PICTURE_MAX_COUNT = 5
    const val BUSINESS_PROFILE_MAX_COUNT = 5
    const val MY_NEIGHBORS_RADIUS_IN_METERS = 80_467.2 //50 miles
    const val MY_NEIGHBORS_MARKER_TITLE_MAX_LENGTH = 12
    const val MY_NEIGHBORS_CLUSTER_MAX_COUNT = 99
    const val STORY_DESCRIPTION_MIN_LENGTH = 2
    const val STORY_DESCRIPTION_MAX_LENGTH = 300
    const val STORY_MIN_DURATION = 1000L
    const val STORY_DEFAULT_COVER_TIMESTAMP = 1000L
    const val AUDIO_DESCRIPTION_MIN_LENGTH = 1
    const val AUDIO_DESCRIPTION_MAX_LENGTH = 50
    const val AUDIO_MAX_DURATION = 5 * 60 * 1000

    // Hashtags
    const val REGEX_HASHTAG = "#(\\w{1,30})"
    const val REGEX_MENTION_FE = "@(\\w{1,30})"
    const val REGEX_MENTION_BE = "\\[[^\\]\\[]+\\|\\d+\\]"

    //FFmpeg
    const val VIDEO_FROM_IMAGE_DURATION = 3

}