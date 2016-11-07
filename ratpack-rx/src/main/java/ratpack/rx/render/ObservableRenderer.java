/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ratpack.rx.render;

import ratpack.handling.Context;
import ratpack.jackson.Jackson;
import ratpack.render.RendererSupport;
import rx.Observable;

import static ratpack.rx.RxRatpack.promise;

public class ObservableRenderer extends RendererSupport<Observable<?>> {

    @Override
    public void render(Context ctx, Observable<?> observable) throws Exception {
        doRender(ctx, observable);
    }

    public static void doRender(Context ctx, Observable<?> observable) {
        promise(observable).map(Jackson::json).then(ctx::render);
    }
}
