package net.dean.jraw.pagination

import net.dean.jraw.RedditClient
import net.dean.jraw.http.HttpRequest

/**
 * This class, like its name suggests, supports fewer query modifiers compared to other Paginators. Only the limit can
 * be set.
 */
open class BarebonesPaginator<T> private constructor(
    reddit: RedditClient,
    baseUrl: String,
    limit: Int,
    clazz: Class<T>
) : Paginator<T>(reddit, baseUrl, limit, clazz) {

    override fun createNextRequest(): HttpRequest {
        val args = mutableMapOf("limit" to limit.toString())
        if (current?.nextName != null)
            args.put("after", current!!.nextName!!)

        return reddit.requestStub()
            .path(baseUrl)
            .query(args)
            .build()
    }

    class Builder<T>(reddit: RedditClient, baseUrl: String, clazz: Class<T>) :
        Paginator.Builder<T>(reddit, baseUrl, clazz) {

        private var limit: Int = Paginator.DEFAULT_LIMIT
        fun limit(limit: Int): Builder<T> { this.limit = limit; return this }

        override fun build(): BarebonesPaginator<T> =
            BarebonesPaginator(reddit, baseUrl, limit, clazz)

        companion object {
            inline fun <reified T> create(reddit: RedditClient, baseUrl: String): Builder<T> {
                return Builder(reddit, baseUrl, T::class.java)
            }
        }
    }
}
