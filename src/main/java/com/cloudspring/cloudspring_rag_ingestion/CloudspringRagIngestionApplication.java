package com.cloudspring.cloudspring_rag_ingestion;

import com.cloudspring.cloudspring_rag_ingestion.config.ChunkingProperties;
import com.cloudspring.cloudspring_rag_ingestion.config.IngestionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		ChunkingProperties.class,
		IngestionProperties.class
})
public class CloudspringRagIngestionApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudspringRagIngestionApplication.class, args);
	}

}
