package org.example;

import com.google.auto.service.AutoService;
import java.util.Map;
import org.apache.flink.statefun.flink.io.spi.FlinkIoModule;

@AutoService(FlinkIoModule.class)
public class PlaygroundFlinkIoModule implements FlinkIoModule {
  @Override
  public void configure(Map<String, String> globalConfiguration, Binder binder) {
    binder.bindSourceProvider(Constants.INGRESS_TYPE, new PlaygroundSourceProvider());
//    binder.bindSinkProvider(Constants.EGRESS_TYPE, new PlaygroundSinkProvider());
  }
}
