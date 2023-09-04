import dto.GitHubDataRetriever;
import entities.RateLimit;
import entities.RepositoryIssues;

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



      executeProcessWithUnlimitedRequests();

    }
    catch(Exception ex)
    {
      System.out.println("[Error] " + ex.getMessage());
    }
  }

  private static void executeProcessWithUnlimitedRequests() throws Exception
  {
    List<String> repositories = GitHubDataRetriever.getAllRepositories(repositoryFilePath);

    for(int i = 0; i < repositories.size(); i++)
    {

      // Fetch Rate Limit
      RateLimit rateLimit = GitHubDataRetriever.getRateLimit();


      String repository = repositories.get(i);

      String outputReportName = outputReportPath + "/" + outputReportFileName + "-" + (i + 1) + ".csv";
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

          // Fetch Issues from repository
          List<RepositoryIssues> repositoryIssues = GitHubDataRetriever.fetchAllRepositoryIssues(repository);

          // Generate Report
          GitHubDataRetriever.generateReport(repositoryIssues,outputReportName);

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
