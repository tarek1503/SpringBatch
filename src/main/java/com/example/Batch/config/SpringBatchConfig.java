package com.example.Batch.config;


import com.example.Batch.dao.CustomerRepository;
import com.example.Batch.entity.Customer;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;


@Configuration
@EnableBatchProcessing
@AllArgsConstructor
@EnableAutoConfiguration
public class SpringBatchConfig {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    private CustomerRepository customerRepository;


    @Bean
    public FlatFileItemReader<Customer> reader(){
        FlatFileItemReader<Customer> itemReader= new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource("src/main/resources/Customers.csv"));
        itemReader.setName("csvReader");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapper());
        return itemReader;
    }
    private LineMapper<Customer> lineMapper() {
        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer lineTokenizer=new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("firstName","lastName","email","gender","contactNo","country","dob");
        BeanWrapperFieldSetMapper fieldSetMapper = new BeanWrapperFieldSetMapper();
        fieldSetMapper.setTargetType(Customer.class);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        lineMapper.setLineTokenizer(lineTokenizer);

        return lineMapper;
    }


    @Bean
    public RepositoryItemWriter<Customer> writer(){
        RepositoryItemWriter<Customer> writer = new RepositoryItemWriter<>();
        writer.setRepository(customerRepository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public CustomerMaleProcessor maleProcessor(){

        return new CustomerMaleProcessor();
    }
    @Bean
    public CustomerFemaleProcessor femaleProcessor(){
        return new CustomerFemaleProcessor();
    }
    @Bean
    public CustomerProcessor processor(){
        return new CustomerProcessor();
    }


    @Bean
    public Step stepOne(){
        return stepBuilderFactory.get("csv-step1").<Customer,Customer>chunk(10)
                .reader(reader())
                .processor(maleProcessor())
                .writer(writer())
                .build();
    }
    @Bean
    public Step stepTwo(){
        return stepBuilderFactory.get("csv-step2").<Customer,Customer>chunk(10)
                .reader(reader())
                .processor(femaleProcessor())
                .writer(writer())
                .build();
    }

    @Bean
    public Step stepThree(){
        return stepBuilderFactory.get("csv-step3").<Customer,Customer>chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    public Job runJob(){
        return jobBuilderFactory.get("importCustomers")
                .start(stepThree()).build();
    }
}
