/*
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License i distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package com.xiaonei.jade.dbplugin.listener;


import com.xiaonei.jade.dbplugin.configuration.ConfigJudge;
import com.xiaonei.jade.dbplugin.model.DataModel;
import com.xiaonei.jade.dbplugin.writer.DataMonitorWriter;

/**
 * DataMonitorListener <br>
 * 监听数据
 * 
 * @author tai.wang@opi-corp.com May 13, 2010 - 7:39:07 PM
 */
public interface DataMonitorListener {

    /**
     * listen<br>
     * 获得数据
     * 
     * @param data {@link DataModel}
     * 
     * @author tai.wang@opi-corp.com Jul 16, 2010 - 12:16:42 PM
     */
    public void listen(DataModel data);

    /**
     * setWriter<br>
     * 
     * @param writer {@link DataMonitorWriter}
     * 
     * @author tai.wang@opi-corp.com Jul 16, 2010 - 12:17:14 PM
     */
    public void setWriter(DataMonitorWriter writer);

    /**
     * setConfigJudge<br>
     * 
     * @param judge {@link ConfigJudge}
     * 
     * @author tai.wang@opi-corp.com Jul 16, 2010 - 2:02:32 PM
     */
    void setConfigJudge(ConfigJudge judge);
}
