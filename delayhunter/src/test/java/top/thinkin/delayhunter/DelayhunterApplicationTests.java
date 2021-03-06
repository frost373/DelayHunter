package top.thinkin.delayhunter;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
class Test {
    static OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static final MetricRegistry metrics = new MetricRegistry();
    private static final Meter requests = metrics.meter("requests");

    static void startReport() {
        ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(3, TimeUnit.SECONDS);
    }

    public static void main(String[] args) {
        String url = args[0];
        contextLoads( url);
    }
     static void contextLoads(String urlx) {
        startReport();

        String url = "http://"+urlx+"/add";

        for (int i = 0; i < 100 * 10000; i++) {
            handle(url);
        }

    }

    private static void handle(String url) {
        RequestBody body = RequestBody.create("{\n" +
                "    \"id\": \""+ UUID.randomUUID().toString() +"\",\n" +
                "    \"data\": \"{\\\"task\\\":\\\"do "+ UUID.randomUUID().toString() +"some \\\",\\\"num\\\":102081}\",\n" +
                "    \"millisecond\": 1000,\n" +
                "    \"group\":\"test\"\n" +
                "}", JSON);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

         Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                log.error("error",e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                MessageK message =  Jsons.readValue(response.body().string(),MessageK.class);
                if(message.errno !=0){
                    log.error("error",Jsons.writeAsString(message));
                }else {
                    requests.mark();
                }
            }
        });
    }

}
