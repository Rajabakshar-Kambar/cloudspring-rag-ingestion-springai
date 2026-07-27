package com.cloudspring.cloudspring_rag_ingestion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;

@Configuration
public class AwsConfig {

    @Bean
    public S3Client s3Client() {

        return S3Client.builder()
                .region(Region.AP_SOUTH_1)
                .build();
    }

    @Bean
    public JsonMapper jsonMapper() {
        return JsonMapper.builder().build();
    }

    // BedrockRuntimeClient is no longer created here.
    // spring-ai-starter-model-bedrock auto-configures its own Bedrock clients
    // (sync + async) from the spring.ai.bedrock.aws.* properties and wires them
    // into the EmbeddingModel bean for you.
}
