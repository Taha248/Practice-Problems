package dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import entities.RateLimit;
import entities.RepositoryIssues;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class GitHubDataRetriever {
    public static final String ACCESS_TOKEN = "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";

    public static RateLimit getRateLimit()
    {
        RateLimit rateLimit = null;
        try
        {

            String apiUrl = "https://api.github.com/rate_limit";
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "token " + ACCESS_TOKEN);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                ObjectMapper objectMapper = new ObjectMapper();

                JsonNode rootNode = objectMapper.readTree(response.toString());

                int limit = rootNode.get("resources").get("core").get("limit").asInt();
                int used = rootNode.get("resources").get("core").get("used").asInt();
                int remaining = rootNode.get("resources").get("core").get("remaining").asInt();
                int reset = rootNode.get("resources").get("core").get("reset").asInt();

                rateLimit = new RateLimit(reset, remaining, limit, used);

//                System.out.println(response);
            } else {
                System.out.println("Failed to fetch rate limit. Response code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rateLimit;
    }
    public static void fetchAllRepositoryIssues(String repository, String outputReportName) throws Exception {
        String[] repos = repository.split("/");
        String owner = repos[repos.length - 2];
        String repo = repos[repos.length - 1];

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("https://api.github.com/repos/" + owner + "/" + repo + "/issues").header("Authorization", "Bearer " + ACCESS_TOKEN).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    ObjectMapper objectMapper = new ObjectMapper();

                    List<RepositoryIssues> issues = objectMapper.readValue(responseBody.string(), objectMapper.getTypeFactory().constructCollectionType(List.class, RepositoryIssues.class));

                    // Now, 'issues' contains a list of GitHubIssue objects
                    try (FileWriter writer = new FileWriter( outputReportName); ICSVWriter csvWriter = new CSVWriterBuilder(writer).withSeparator(',').withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER).withEscapeChar(CSVWriter.NO_ESCAPE_CHARACTER).build()) {

                        List<String> header = Arrays.asList("Issue Title", "Issue Details", "Total Commments", "Author", "Modified Date", "Created Date");

                        csvWriter.writeNext(header.toArray(new String[0]));

                        for (RepositoryIssues issue : issues) {
                            SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                            SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

                            Date dt_update = inputDateFormat.parse(issue.getUpdated_at());
                            Date dt_create = inputDateFormat.parse(issue.getCreated_at());
                            String str_update = outputDateFormat.format(dt_update);
                            String str_create = outputDateFormat.format(dt_create);

                            String[] issueReport = new String[]{escapeCSVCell(issue.getTitle()), escapeCSVCell(issue.getBody()), escapeCSVCell(issue.getComments()), escapeCSVCell(issue.getUser().getLogin()), str_update, str_create};

                            csvWriter.writeNext(issueReport);
                        }
                    }

                    System.out.println("Issue Report Generated ! .......");


                }
            } else {
                System.err.println("Failed to fetch issues. Status code: " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    public static List<String> getAllRepositories(String repositoryFilePath) throws FileNotFoundException {
        InputStream inputStream = new FileInputStream(repositoryFilePath);
        List<String> repositories = new ArrayList<>();

        if (inputStream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    repositories.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("File not found: " + repositoryFilePath);
        }
        return repositories;
    }

    private static String escapeCSVCell(String cellContent) {
        return "\"" + cellContent.replace("\"", "\"\"") + "\"";
    }
}
