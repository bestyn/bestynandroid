package com.gbksoft.neighbourhood.ui.fragments.stories.create_story

import android.net.Uri
import androidx.core.net.toUri
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.ExecuteCallback
import com.arthenica.mobileffmpeg.FFmpeg
import com.gbksoft.neighbourhood.app.NApplication
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.add_text.StoryTextModel
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.record.FFmpegListener
import com.gbksoft.neighbourhood.utils.media.MediaUtils
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.concurrent.Executors

object FFmpegUtil {

    fun trimDuetVideo(video: Uri, startTimeMills: Int): Uri? {
        val storyDirPath = getStoryResDirectory()
        val resPath = "${storyDirPath}/trimmed_duet_${MediaUtils.generateVideoFileName()}"
        val resFile = File(resPath)
        if (resFile.exists()) {
            resFile.delete()
        }
        val startTime = stringForTime(startTimeMills)

        Timber.tag("Start")

        val command = "-ss $startTime -i \"${video.path}\" -vcodec copy -avoid_negative_ts 1 ${resFile.path}"
        return executeCommand(command, resFile)
    }

    fun scaleDuetVideo(video: Uri): Uri? {
        val storyDirPath = getStoryResDirectory()
        val resPath = "${storyDirPath}/scalled_duet_${MediaUtils.generateVideoFileName()}"
        val resFile = File(resPath)
        if (resFile.exists()) {
            resFile.delete()
        }

        val command = "-i ${video.path} -filter:v \"scale=900:-2,setsar=1:1\" -r 30 -c:v libx264 -preset ultrafast -c:a aac ${resFile.path}"
        return executeCommand(command, resFile)
    }

    fun combineDuetVideo(topVideo: Uri, bottomVideo: Uri): Uri? {
        val storyDirPath = getStoryResDirectory()
        val resPath = "${storyDirPath}/combined_duet_${MediaUtils.generateVideoFileName()}"
        val resFile = File(resPath)
        if (resFile.exists()) {
            resFile.delete()
        }

        val command = "-i ${topVideo.path} -i ${bottomVideo.path} -filter_complex \"[0:v][1:v]vstack=inputs=2:shortest=1[v];[0:a][1:a]amerge=inputs=2,pan=stereo|c0<c0+c1|c1<c2+c3[a]\" -map \"[v]\" -map \"[a]\"  ${resFile.path}"
        return executeCommand(command, resFile)
    }

    fun cropDuetVideo(video: Uri): Uri? {
        val storyDirPath = getStoryResDirectory()
        val resPath = "${storyDirPath}/cropped_duet_${MediaUtils.generateVideoFileName()}"
        val resFile = File(resPath)
        if (resFile.exists()) {
            resFile.delete()
        }

        val dstHeightToWidth = 8 / 9.0
        val command = "-i ${video.path} -filter:v \"crop=iw:iw*$dstHeightToWidth:0:(ih-iw*$dstHeightToWidth)/2\" -r 30 -c:v libx264 -preset ultrafast -c:a aac ${resFile.path}"
        return executeCommand(command, resFile)
    }

    fun addAudioToVideo(video: Uri, audio: Uri, videoVolume: Float, audioVolume: Float): Uri? {
        val storyDirPath = getStoryResDirectory()
        val resPath = "${storyDirPath}/${MediaUtils.generateVideoFileName()}"
        val resFile = File(resPath)
        if (resFile.exists()) {
            resFile.delete()
        }

        val command = "-y -stream_loop -1 -i \"${audio.path}\" -i \"${video.path}\" -filter_complex \"[0:a]volume=$audioVolume[a1];[1:a]volume=$videoVolume[a2];[a1][a2]amerge,pan=stereo|c0<c0+c2|c1<c1+c3[out]\" -map 1:v -map \"[out]\" -c:v copy -c:a aac -shortest ${resFile.path}"
        return executeCommand(command, resFile)
    }

    fun addImagesToVideo(video: Uri, imagePathList: List<StoryTextModel>): Uri? {
        val storyDirPath = getStoryResDirectory()
        val resPath = "${storyDirPath}/${MediaUtils.generateVideoFileName()}"
        val resFile = File(resPath)
        if (resFile.exists()) {
            resFile.delete()
        }

        val images = getImagesInputCommand(imagePathList.map { it.imagePath })
        val overlay = getImagesOverlayCommand(imagePathList)
        val command = "-y -i \"${video.path}\" $images-filter_complex \"$overlay\" -codec:a copy ${resFile.path}"
        return executeCommand(command, resFile)
    }

    private fun getImagesOverlayCommand(storyTextModels: List<StoryTextModel>): String {
        val sb = StringBuilder()
        storyTextModels.forEachIndexed { pos, storyTextModel ->
            sb.append("overlay=x=${storyTextModel.x}:y=${storyTextModel.y}")
            if (storyTextModel.startTime != -1 && storyTextModel.endTime != -1) {
                val startTime = storyTextModel.startTime / 1000
                val endTime = storyTextModel.endTime / 1000
                sb.append(":enable='between(t,$startTime,$endTime)'")
            }
            if (pos < storyTextModels.size - 1) {
                sb.append(",")
            }
        }
        return sb.toString()
    }

    private fun getImagesInputCommand(imagePathList: List<String>): String {
        val sb = StringBuilder()
        imagePathList.forEach { path ->
            sb.append("-i $path ")
        }
        return sb.toString()
    }

    fun createVideoFromImage(imagePath: String, duration: Int): Uri? {
        val storyDirPath = getStoryResDirectory()
        val resPath = "${storyDirPath}/${MediaUtils.generateVideoFileName()}"
        val resFile = File(resPath)
        if (resFile.exists()) {
            resFile.delete()
        }

        val command = "-y -loop 1 -framerate 1 -i \"$imagePath\" -t $duration -preset ultrafast -b:v 2000k -maxrate 2000k -r 30 -g 12 -c:a copy -pix_fmt yuv420p ${resFile.path}"
        return executeCommand(command, resFile)
    }

    fun removeAudio(video: Uri): Uri? {
        val storyDirPath = getStoryResDirectory()
        val resPath = "${storyDirPath}/story_muted.mp4"
        val resFile = File(resPath)
        if (resFile.exists()) {
            resFile.delete()
        }

        val command = "-y -i \"${video.path}\" -c copy -an ${resFile.path}"
        return executeCommand(command, resFile)
    }

    fun trimVideo(video: Uri, resVideoName: String, startTimeMills: Int, endTimeMills: Int): Uri? {
        val storyDirPath = getStoryResDirectory()
        val resPath = "${storyDirPath}/$resVideoName.mp4"
        val resFile = File(resPath)
        if (resFile.exists()) {
            resFile.delete()
        }
        val startTime = stringForTime(startTimeMills)
        val endTime = stringForTime(endTimeMills)

        Timber.tag("Start")

        val command = "-y -ss $startTime -to $endTime -i \"${video.path}\" -vcodec copy ${resFile.path}"
        return executeCommand(command, resFile)
    }

    fun trimAudio(audio: Uri, startTimeMills: Int, endTimeMills: Int): Uri? {
        val storyDirPath = getStoryResDirectory()
        val resPath = "${storyDirPath}/${MediaUtils.generateFileName("trimmed_audio")}.mp3"
        val resFile = File(resPath)
        if (resFile.exists()) {
            resFile.delete()
        }
        val startTime = stringForTime(startTimeMills)
        val endTime = stringForTime(endTimeMills)

        Timber.tag("Start")

        val command = "-y -ss $startTime -to $endTime -i \"${audio.path}\" -c copy ${resFile.path}"
        return executeCommand(command, resFile)

    }

    private fun stringForTime(timeMs: Int): String {
        val millis = timeMs % 1000
        val second = timeMs / 1000 % 60
        val minute = timeMs / (1000 * 60) % 60
        val hour = timeMs / (1000 * 60 * 60) % 24

        return String.format("%02d:%02d:%02d.%d", hour, minute, second, millis)
    }

    fun concatenate(videoSegments: List<Uri>): Uri? {
        val file = generateSegmentsFile(videoSegments) ?: return null
        val storyDirPath = getStoryResDirectory()
        val resPath = "${storyDirPath}/${MediaUtils.generateVideoFileName()}"
        val resFile = File(resPath)
        if (resFile.exists()) {
            resFile.delete()
        }

        val command = "-y -f concat -safe 0 -i ${file.path} -c copy ${resFile.path}"
        return executeCommand(command, resFile)
    }

    fun concatVideoFilter(videoSegments: List<Uri>): Uri? {
        val storyDirPath = getStoryResDirectory()
        val resPath = "${storyDirPath}/${MediaUtils.generateVideoFileName()}"
        val resFile = File(resPath)
        if (resFile.exists()) {
            resFile.delete()
        }

        val inputs = videoSegments.map { "\"${it.path}\"" }.joinToString(prefix = "-i ", separator = " -i ")
        val filterInputs = videoSegments.mapIndexed { i, _ -> "[$i:v] [$i:a]" }.joinToString(separator = " ")

        val command =
                "$inputs -filter_complex \"$filterInputs concat=n=${videoSegments.size}:v=1:a=1 [v] [a]\" -map \"[v]\" -map \"[a]\" -r 30 -c:v libx264 -preset ultrafast -c:a aac ${resFile.path}"
        return executeCommand(command, resFile)
    }

    fun resizeImage(imagePath: String): Uri? {
        val storyDirPath = getStoryResDirectory()
        val resPath = "${storyDirPath}/${MediaUtils.generatePhotoFileName()}"
        val resFile = File(resPath)
        if (resFile.exists()) {
            resFile.delete()
        }

        val command = "-y -i \"${imagePath}\" -vf \"scale=min(iw*1600/ih\\,900):min(1600\\,ih*900/iw),setsar=1:1,pad=900:1600:(900-iw)/2:(1600-ih)/2\" -preset ultrafast -c:a copy -pix_fmt yuv420p ${resFile.path}"
        return executeCommand(command, resFile)
    }

    fun resize(video: Uri): Uri? {
        val storyDirPath = getStoryResDirectory()
        val resPath = "${storyDirPath}/resized_video_${MediaUtils.generateVideoFileName()}"
        val resFile = File(resPath)
        if (resFile.exists()) {
            resFile.delete()
        }

        val command = "-y -i \"${video.path}\" -vf \"scale=min(iw*1600/ih\\,900):min(1600\\,ih*900/iw),setsar=1:1,pad=900:1600:(900-iw)/2:(1600-ih)/2\" -preset ultrafast -c:a copy -pix_fmt yuv420p ${resFile.path}"
        return executeCommand(command, resFile)
    }

    fun addSilentAudio(video: Uri): Uri? {
        val storyDirPath = getStoryResDirectory()
        val resPath = "${storyDirPath}/${MediaUtils.generateVideoFileName()}"
        val resFile = File(resPath)
        if (resFile.exists()) {
            resFile.delete()
        }

        val command = "-y -f lavfi -i anullsrc -i \"${video.path}\" -c:v copy -c:a aac -map 0:a -map 1:v -shortest ${resFile.path}"
        return executeCommand(command, resFile)
    }

    fun clearTempFiles() {
        val root = File(NApplication.context.filesDir, "story")
        deleteRecursive(root)

    }

    private fun deleteRecursive(file: File) {
        if (file.isDirectory) {
            file.listFiles()?.forEach { deleteRecursive(it) }
        }
        val isDeleted = file.delete()
        Timber.tag("FFmpegUtil").d("File deleted: $isDeleted, file: ${file.path}")
    }

    private fun generateSegmentsFile(videoSegments: List<Uri>): File? {
        try {
            val root = File(NApplication.context.filesDir, "story")
            if (!root.exists()) {
                root.mkdirs()
            }
            val segmentsFile = File(root, "video_segments")
            val writer = FileWriter(segmentsFile)
            videoSegments.forEach {
                writer.append("file " + it.path + '\n')
            }
            writer.flush()
            writer.close()
            return segmentsFile
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    private fun getStoryResDirectory(): String {
        val root = File(NApplication.context.filesDir, "story")
        if (!root.exists()) {
            root.mkdirs()
        }
        return root.path
    }

    private val executor = Executors.newSingleThreadExecutor()

    private fun executeCommand(command: String, resFile: File, callback: FFmpegListener) {
        Timber.tag("FFMPEG").d("Command $command")
        val executeCallback = ExecuteCallback { executionId, returnCode ->
            when (returnCode) {
                Config.RETURN_CODE_SUCCESS -> {
                    Timber.tag("FFMPEG").d("Success")
                    callback.onSuccess(resFile.toUri())
                }
                Config.RETURN_CODE_CANCEL -> {
                    Timber.tag("FFMPEG").d("Failure")
                }
                else -> {
                    callback.onError()
                }
            }
        }
        FFmpeg.executeAsync(command, executeCallback, executor)

    }

    fun toMp3(audioPath: Uri): Uri? {
        val command = "ffmpeg -i ${audioPath} -b:a 192K -vn ${audioPath}.mp3"

        val res = FFmpeg.execute(command)
        if (res == Config.RETURN_CODE_SUCCESS) {
            "${audioPath}.mp3".toUri()
        }
        return null
    }


    private fun executeCommand(command: String, resFile: File): Uri? {
        Timber.tag("FFMPEG").d("Command $command")
        val res = FFmpeg.execute(command)
        return if (res == Config.RETURN_CODE_SUCCESS) {
            resFile.toUri()
        } else {
            null
        }
    }
}