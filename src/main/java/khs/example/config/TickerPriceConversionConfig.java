package khs.example.config;
import java.net.MalformedURLException;

import khs.example.elements.LogItemWriter;
import khs.example.elements.TickerData;
import khs.example.elements.TickerFieldSetMapper;
import khs.example.elements.TickerPriceProcessor;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.batch.item.file.transform.FormatterLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;

 
@Configuration
@EnableBatchProcessing  
public class TickerPriceConversionConfig {
 
    @Autowired
    private JobBuilderFactory jobs;
 
    @Autowired
    private StepBuilderFactory steps;
 
    @Bean
    public ItemReader<TickerData> reader() throws MalformedURLException {
        FlatFileItemReader<TickerData> reader = new FlatFileItemReader<TickerData>();
        reader.setResource(new UrlResource("http://finance.yahoo.com/d/quotes.csv?s=XOM+IBM+JNJ+MSFT&amp;f=snd1ol1p2"));
        reader.setLineMapper(new DefaultLineMapper<TickerData>() {{
            setLineTokenizer(new DelimitedLineTokenizer());
            setFieldSetMapper(new TickerFieldSetMapper());
        }});
        return reader;
    }
 
    @Bean
    public ItemProcessor<TickerData, TickerData> processor() {
        return new TickerPriceProcessor();
    }
 
    @Bean
    public ItemWriter<TickerData> writer() {
        return new LogItemWriter();
    }
    
    @Bean
    public ItemWriter<TickerData> fileWriter() {
    	
    	FlatFileItemWriter<TickerData> writer = new FlatFileItemWriter<>();
    	writer.setName("stock-export.sdf"); 	
    	
    	FormatterLineAggregator<TickerData> fla = new FormatterLineAggregator<>();
    	FieldExtractor<TickerData> fe = new FieldExtractor<TickerData>() {
			public Object[] extract(TickerData item) {
				return new Object[] {item.getSymbol(),item.getChangePct(),item.getLastTrade()};
				};   
    	};
    	   	
    	fla.setFieldExtractor(fe);
    	fla.setFormat("%-5s,%-9s,%-2.2f");
    	writer.setLineAggregator(fla);
    	writer.setResource(new FileSystemResource( "/users/dpitt/stock-export.sdf"));
        return writer;
    }
   
 
    @Bean
    public Job TickerPriceConversion() throws MalformedURLException {
        return jobs.get("TickerPriceConversion").start(convertPrice()).next(exportFile()).build();
    }
 
    @Bean
    public Step convertPrice() throws MalformedURLException {
        return steps.get("convertPrice")
                .<TickerData, TickerData> chunk(5)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }
    
    @Bean
    public Step exportFile() throws MalformedURLException {
        return steps.get("exportPDF")
                .<TickerData, TickerData> chunk(5)
                .reader(reader())
                .processor(processor())
                .writer(fileWriter())
                .build();
    }
    
    
    
}