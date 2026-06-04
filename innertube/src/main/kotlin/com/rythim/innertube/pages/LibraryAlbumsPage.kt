package com.rythim.innertube.pages

import com.rythim.innertube.models.Album
import com.rythim.innertube.models.AlbumItem
import com.rythim.innertube.models.Artist
import com.rythim.innertube.models.ArtistItem
import com.rythim.innertube.models.MusicResponsiveListItemRenderer
import com.rythim.innertube.models.MusicTwoRowItemRenderer
import com.rythim.innertube.models.PlaylistItem
import com.rythim.innertube.models.SongItem
import com.rythim.innertube.models.YTItem
import com.rythim.innertube.models.oddElements
import com.rythim.innertube.utils.parseTime

data class LibraryAlbumsPage(
    val albums: List<AlbumItem>,
    val continuation: String?,
) {
    companion object {
        fun fromMusicTwoRowItemRenderer(renderer: MusicTwoRowItemRenderer): AlbumItem? {
            return AlbumItem(
                        browseId = renderer.navigationEndpoint.browseEndpoint?.browseId ?: return null,
                        playlistId = renderer.thumbnailOverlay?.musicItemThumbnailOverlayRenderer?.content
                            ?.musicPlayButtonRenderer?.playNavigationEndpoint
                            ?.watchPlaylistEndpoint?.playlistId ?: return null,
                        title = renderer.title.runs?.firstOrNull()?.text ?: return null,
                        artists = null,
                        year = renderer.subtitle?.runs?.lastOrNull()?.text?.toIntOrNull(),
                        thumbnail = renderer.thumbnailRenderer.musicThumbnailRenderer?.getThumbnailUrl() ?: return null,
                        explicit = renderer.subtitleBadges?.find {
                            it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                        } != null
                    )
        }
    }
}
