package com.watchers.scheduler;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import com.watchers.config.AutoWiringSpringBeanJobFactory;
import com.watchers.config.SettingConfiguration;
import com.watchers.scheduler.job.ContinentalshiftTimerJob;
import com.watchers.scheduler.job.ProcessingUnitJob;
import com.watchers.scheduler.job.SaveTimerJob;
import com.watchers.scheduler.job.TurnTimerJob;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

@Configuration
@EnableAutoConfiguration
@ConditionalOnExpression("'${using.spring.schedulerFactory}'=='true'")
public class SpringQrtzScheduler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SettingConfiguration settingConfiguration;

    @PostConstruct
    public void init() {
        logger.info("Spring Quartz Schedular initiated");
    }

    @Bean
    public SpringBeanJobFactory springBeanJobFactory() {
        AutoWiringSpringBeanJobFactory jobFactory = new AutoWiringSpringBeanJobFactory();
        logger.debug("Configuring Job factory");

        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean(name="turnScheduler")
    public SchedulerFactoryBean turnScheduler(@Qualifier("turnTimerTrigger") Trigger trigger, @Qualifier("turnTimerJob") JobDetail job, DataSource quartzDataSource) {
        return getSchedulerFactoryBean(trigger, job, quartzDataSource);
    }

    @Bean(name="continentalshiftScheduler")
    public SchedulerFactoryBean continentalshiftScheduler(@Qualifier("continentalshiftTimerTrigger") Trigger trigger, @Qualifier("continentalshiftTimerJob") JobDetail job, DataSource quartzDataSource) {
        return getSchedulerFactoryBean(trigger, job, quartzDataSource);
    }

    @Bean(name="processScheduler")
    public SchedulerFactoryBean processScheduler(@Qualifier("processTrigger") Trigger trigger, @Qualifier("processJob") JobDetail job, DataSource quartzDataSource) {
        return getSchedulerFactoryBean(trigger, job, quartzDataSource);
    }

    @Bean(name="saveScheduler")
    public SchedulerFactoryBean saveScheduler(@Qualifier("saveTimerTrigger") Trigger trigger, @Qualifier("saveTimerJob") JobDetail job, DataSource quartzDataSource) {
        return getSchedulerFactoryBean(trigger, job, quartzDataSource);
    }

    private SchedulerFactoryBean getSchedulerFactoryBean(Trigger trigger, JobDetail job, DataSource quartzDataSource) {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setConfigLocation(new ClassPathResource("quartz.properties"));

        logger.debug("Setting the Scheduler up");
        schedulerFactory.setJobFactory(springBeanJobFactory());
        schedulerFactory.setJobDetails(job);
        schedulerFactory.setTriggers(trigger);

        // Comment the following line to use the default Quartz job store.
        schedulerFactory.setDataSource(quartzDataSource);

        return schedulerFactory;
    }

    @Bean(name="turnTimerJob")
    public JobDetailFactoryBean jobDetailTurn() {
        Class<? extends Job> jobClass = TurnTimerJob.class;
        String name = "Turn";

        return getJobDetailFactoryBean(jobClass, name);
    }

    @Bean(name="continentalshiftTimerJob")
    public JobDetailFactoryBean jobDetailContinentalshift() {
        Class<? extends Job> jobClass = ContinentalshiftTimerJob.class;
        String name = "Continentalshift";

        return getJobDetailFactoryBean(jobClass, name);
    }

    @Bean(name="saveTimerJob")
    public JobDetailFactoryBean jobDetailSave() {
        Class<? extends Job> jobClass = SaveTimerJob.class;
        String name = "Save";

        return  getJobDetailFactoryBean(jobClass, name);
    }

    @Bean(name="processJob")
    public JobDetailFactoryBean jobDetailProcess() {
        Class<? extends Job> jobClass = ProcessingUnitJob.class;
        String name = "Process";

        return  getJobDetailFactoryBean(jobClass, name);
    }

    private JobDetailFactoryBean getJobDetailFactoryBean(Class<? extends Job> jobClass, String name) {
        JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(jobClass);
        jobDetailFactory.setName("Qrtz_Job " + name + " invoker");
        jobDetailFactory.setDescription("Invokes the " + name + " job");
        jobDetailFactory.setDurability(true);
        return jobDetailFactory;
    }

    @Bean(name="turnTimerTrigger")
    public SimpleTriggerFactoryBean turnTrigger(@Qualifier("turnTimerJob") JobDetail job) {
        return getSimpleTriggerFactoryBean(job, settingConfiguration.getTurnTimer());
    }

    @Bean(name="continentalshiftTimerTrigger")
    public SimpleTriggerFactoryBean continentalshiftTrigger(@Qualifier("continentalshiftTimerJob") JobDetail job) {
        return getSimpleTriggerFactoryBean(job, settingConfiguration.getContinentalshiftTimer());
    }

    @Bean(name="saveTimerTrigger")
    public SimpleTriggerFactoryBean saveTrigger(@Qualifier("saveTimerJob") JobDetail job) {
        return getSimpleTriggerFactoryBean(job, settingConfiguration.getSaveTimer());
    }

    @Bean(name="processTrigger")
    public SimpleTriggerFactoryBean processTrigger(@Qualifier("processJob") JobDetail job) {
        return getSimpleTriggerFactoryBean(job, settingConfiguration.getProcessingTimer());
    }

    private SimpleTriggerFactoryBean getSimpleTriggerFactoryBean(JobDetail job, int frequencyInSec) {
        SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
        trigger.setJobDetail(job);

        logger.info("Configuring trigger to fire every {} seconds", frequencyInSec);

        trigger.setRepeatInterval(frequencyInSec * 1000);
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        trigger.setName("Qrtz_Trigger");
        return trigger;
    }

    @Bean
    @QuartzDataSource
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource quartzDataSource() {
        return DataSourceBuilder.create().build();
    }

}