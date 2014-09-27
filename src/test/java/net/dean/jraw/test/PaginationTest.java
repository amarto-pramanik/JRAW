package net.dean.jraw.test;

import net.dean.jraw.JrawUtils;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.core.Listing;
import net.dean.jraw.models.core.Submission;
import net.dean.jraw.models.core.Thing;
import net.dean.jraw.pagination.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

/**
 * This class tests all concrete subclasses of {@link net.dean.jraw.pagination.Paginator}
 */
public class PaginationTest extends AuthenticatedRedditTest {

    @Test
    public void testSubredditPaginatorFrontPage() throws NetworkException {
        SubredditPaginator frontPage = new SubredditPaginator(reddit);
        commonTest(frontPage);
    }

    @Test
    public void testSubredditPaginatorSubreddit() throws NetworkException {
        SubredditPaginator pics = new SubredditPaginator(reddit, "pics");
        commonTest(pics);
    }

    @Test
    public void testSearchPaginator() throws NetworkException {
        SearchPaginator paginator = new SearchPaginator(reddit, "test");
        String subreddit = "AskReddit";
        paginator.setSubreddit(subreddit);
        commonTest(paginator);

        for (Submission s : paginator.getCurrentListing()) {
            // Make sure they all come from the same subreddit
            Assert.assertTrue(s.getSubredditName().equals(subreddit));
        }
    }

    @Test
    public void testSpecificPaginator() throws NetworkException {
        // It would be easier to declare fullNames as an array, but we want to use List.contains()
        List<String> fullNames = Arrays.asList("t3_92dd8", "t3_290287", "t3_28zy98", "t3_28zh9i");
        SpecificPaginator paginator = new SpecificPaginator(reddit, fullNames.toArray(new String[fullNames.size()]));

        Listing<Submission> submissions = paginator.next();
        for (Submission s : submissions) {
            Assert.assertTrue(fullNames.contains(s.getFullName()));
        }
    }

    @Test
    public void testUserContributionPaginator() throws NetworkException {
        // Test all Where values
        for (UserContributionPaginator.Where where : UserContributionPaginator.Where.values()) {
            UserContributionPaginator paginator = new UserContributionPaginator(reddit, where, account.getFullName());
            commonTest(paginator);
        }
    }

    @Test
    public void testUserSubredditsPaginator() throws NetworkException {
        // Test all Where values
        for (UserSubredditsPaginator.Where where : UserSubredditsPaginator.Where.values()) {
            UserSubredditsPaginator paginator = new UserSubredditsPaginator(account, where);
            commonTest(paginator);
        }
    }

    @Test
    public void testAllSubredditsPaginator() throws NetworkException {
        // Test all Where values
        for (AllSubredditsPaginator.Where where : AllSubredditsPaginator.Where.values()) {
            AllSubredditsPaginator paginator = new AllSubredditsPaginator(reddit, where);
            commonTest(paginator);
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testChangeRequestParamters() {
        AllSubredditsPaginator paginator = new AllSubredditsPaginator(reddit, AllSubredditsPaginator.Where.NEW);
        paginator.next();
        // Modifying the request parameters after the initial request, without calling reset
        paginator.setLimit(Paginator.DEFAULT_LIMIT);
        // Should throw an IllegalStateException
        paginator.next();
    }

    @Test
    public void testResetRequestParameters() {
        AllSubredditsPaginator paginator = new AllSubredditsPaginator(reddit, AllSubredditsPaginator.Where.NEW);
        paginator.next();
        paginator.setLimit(Paginator.DEFAULT_LIMIT);
        // We know it has already started, but just making sure this method works as expected
        if (paginator.hasStarted()) {
            paginator.reset();
        }

        // Should not throw an IllegalStateException
        paginator.next();
    }


    protected <T extends Thing> void commonTest(Paginator<T> p) throws NetworkException {
        // Test that the paginator can retrieve the data
        Listing<T> firstPage = p.next();
        validateModel(firstPage);

        if (firstPage.size() > 0) {
            // Test each model in the first page
            validateModels(firstPage);
        } else {
            JrawUtils.logger().warn("Listing was empty");
        }
    }
}
