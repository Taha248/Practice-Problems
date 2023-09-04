import dto.GitHubDataRetriever;
import entities.RateLimit;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainClass
{

  public static String repositoryFilePath = "D:/repositories.txt";
  public static String outputReportPath = "D:/";
  public static String outputReportFileName = "IssueReport";
  public static int rateManagementMode = 0;

  public static void main(String[] args) throws Exception
  {

    try
    {
      if(args != null && args.length >2 )
      {
        repositoryFilePath = args[0];
        outputReportPath = args[1];
        GitHubDataRetriever.ACCESS_TOKEN = args[2];

      }

      // Fetch Rate Limit
      RateLimit rateLimit = GitHubDataRetriever.getRateLimit();


      executeProcessWithUnlimitedRequests(rateLimit);

    }
    catch(Exception ex)
    {
      System.out.println("[Error] " + ex.getMessage());
    }
  }

  private static void executeProcessWithoutDelay(RateLimit rateLimit) throws Exception
  {
    // Check rate limit headers
    List<String> repositories = GitHubDataRetriever.getAllRepositories(repositoryFilePath);

    for(int i = 0; i < repositories.size(); i++)
    {

      int remainingRequests = rateLimit.getRemainingRequest();
      long resetTime = rateLimit.getResetTime();
      long currentTimestamp = System.currentTimeMillis() / 1000;

      if(remainingRequests == 0)
      {
        long sleepTime = Math.max(0, resetTime - currentTimestamp + 1);
        System.out.println("Rate limit exceeded. Sleeping for " + sleepTime + " seconds.");
        TimeUnit.SECONDS.sleep(sleepTime);
      }
      else
      {
        String repository = repositories.get(i);
        String outputReportName = outputReportPath + "/" + outputReportFileName + "-" + (i + 1) + ".csv";
        GitHubDataRetriever.fetchAllRepositoryIssues(repository, outputReportName);
      }
    }


  }

  private static void executeProcessWithUnlimitedRequests(RateLimit rateLimit) throws Exception
  {
    List<String> repositories = GitHubDataRetriever.getAllRepositories(repositoryFilePath);

    for(int i = 0; i < repositories.size(); i++)
    {

      String repository = repositories.get(i);

      try
      {

        long resetTime = rateLimit.getResetTime();
        long currentTimestamp = Instant.now().getEpochSecond();
        int remainingRequests = rateLimit.getRemainingRequest();

        // Calculating Time Left(in Seconds) For Reset
        long timeLeftForReset = resetTime - currentTimestamp;

        if(remainingRequests == 0 && timeLeftForReset > 0)
        {
          System.out.println("Rate limit exceeded. Sleeping for " + timeLeftForReset + " seconds.");
          Thread.sleep(timeLeftForReset * 1000);
        }
        else
        {
          double delaySecondsPerRequest = (double) timeLeftForReset / (double) remainingRequests;
          String outputReportName = outputReportPath + "/" + outputReportFileName + "-" + (i + 1) + ".csv";
          GitHubDataRetriever.fetchAllRepositoryIssues(repository, outputReportName);
          Thread.sleep((int) Math.ceil(delaySecondsPerRequest) * 1000);
        }
      }
      catch(IOException e)
      {
        System.err.println("Error fetching data for repository: " + repository);
        e.printStackTrace();
      }

    }

  }


}
