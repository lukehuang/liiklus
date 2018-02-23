package com.github.bsideup.liiklus.test;

import com.github.bsideup.liiklus.Application;
import com.github.bsideup.liiklus.protocol.ReactorLiiklusServiceGrpc;
import com.github.bsideup.liiklus.protocol.ReactorLiiklusServiceGrpc.ReactorLiiklusServiceStub;
import com.github.bsideup.liiklus.test.support.LocalStackContainer;
import io.grpc.inprocess.InProcessChannelBuilder;
import lombok.val;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;

import java.util.UUID;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(
        classes = {Application.class, TestConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "grpc.enabled=false",
                "grpc.inProcessServerName=liiklus",
        }
)
public abstract class AbstractIntegrationTest {

    public static final int NUM_PARTITIONS = 32;

    static {
        val localstack = new LocalStackContainer();

        val kafka = new KafkaContainer()
                .withEnv("KAFKA_NUM_PARTITIONS", NUM_PARTITIONS + "");

        Stream.of(kafka, localstack).parallel().forEach(GenericContainer::start);

        System.setProperty("kafka.bootstrapServers", kafka.getBootstrapServers());

        System.setProperty("dynamodb.positionsTable", "positions-" + UUID.randomUUID());
        System.getProperties().putAll(localstack.getProperties());
    }

    @Rule
    public TestName testName = new TestName();

    protected ReactorLiiklusServiceStub stub = ReactorLiiklusServiceGrpc.newReactorStub(
            InProcessChannelBuilder.forName("liiklus").build()
    );
}