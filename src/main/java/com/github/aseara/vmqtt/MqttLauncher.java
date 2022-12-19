package com.github.aseara.vmqtt;

import com.github.aseara.vmqtt.conf.MqttConfig;
import com.github.aseara.vmqtt.verticle.MqttVerticle;
import io.netty.util.internal.StringUtil;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.cli.CLI;
import io.vertx.core.cli.CommandLine;
import io.vertx.core.cli.Option;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

@Slf4j
public class MqttLauncher {

    public static void main(String[] args) {
        Option profile = new Option()
                .setShortName("p")
                .setLongName("profile")
                .setDefaultValue("dev")
                .setDescription("profile to be used, default is dev. This option will be used to " +
                        "set the config file to config-$profile.yaml when option 'conf' is missing.");

        Option conf = new Option()
                .setShortName("c")
                .setLongName("conf")
                .setDescription("config file be used to load yaml config. Config file is loaded by class path loader. " +
                        "When this option is missing the default conf is determined by profile.");

        Option help = new Option()
                .setShortName("h")
                .setLongName("help")
                .setFlag(true)
                .setHelp(true);

        CLI cli = CLI.create("vmqtt")
                .setSummary("A command line interface to run vmqtt.")
                .addOption(profile)
                .addOption(conf)
                .addOption(help);

        CommandLine cmd = cli.parse(Arrays.asList(args));

        if (!cmd.isValid() || cmd.isAskingForHelp()) {
            StringBuilder builder = new StringBuilder();
            cli.usage(builder);
            System.out.println(builder);
            return;
        }

        String configPath = cmd.getOptionValue(conf.getName());
        if (StringUtil.isNullOrEmpty(configPath)) {
            configPath = "classpath:conf-" + cmd.getOptionValue(profile.getName()) + ".yaml";
        }

        MqttConfig config = initConfig(configPath);
        start(config);
    }

    private static MqttConfig initConfig(String configPath) {
        Yaml yaml = new Yaml();
        log.info("load config from file: {}", configPath);
        try {
            InputStream in;
            if (configPath.startsWith("classpath:")) {
                String path = configPath.substring("classpath:".length()).trim();
                in = MqttLauncher.class.getClassLoader().getResourceAsStream(path);
            } else {
                in = new FileInputStream(configPath);
            }

            try (InputStream input = in) {
                return yaml.loadAs(input, MqttConfig.class);
            }
        } catch (IOException e) {
            log.error("can not load config file: {}", configPath, e);
            throw new RuntimeException(e);
        }
    }

    private static void start(MqttConfig config) {
        VertxOptions vertxOptions = new VertxOptions()
                .setPreferNativeTransport(true);

        Vertx vertx = Vertx.vertx(vertxOptions);

        MqttVerticle verticle = new MqttVerticle(config);
        vertx.deployVerticle(verticle).onComplete(ar -> {
            if (ar.succeeded()) {
                log.info("mqtt verticle deploy succeed.");
            } else {
                log.error("mqtt verticle deploy error: ", ar.cause());
                vertx.close();
                System.exit(1);
            }
        });

    }

}
