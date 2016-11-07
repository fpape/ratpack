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

package ratpack.rx.render

import ratpack.rx.RxRatpack
import ratpack.test.internal.RatpackGroovyDslSpec
import rx.Observable
import spock.lang.Subject

import java.util.concurrent.TimeUnit

@Subject(ChunkedObservableRenderer)
class ChunkedObservableRendererSpec extends RatpackGroovyDslSpec {
    def setup() {
        RxRatpack.initialize()
    }

    def "a observable success with single element is rendered as json structure"() {
        given:
        handlers {
            get { ctx ->
                Observable<?> obs = Observable.just([id: 1, value: 'some value'])
                ChunkedObservableRenderer.doRender(ctx, obs)
            }
        }

        when:
        def result = get()
        then:
        rawResponse().startsWith("""HTTP/1.1 200 OK
transfer-encoding: chunked
content-type: application/json
connection: close
""")
        and:
        result.body.text == /[{"id":1,"value":"some value"}]/
    }

    def "a observable success with multiple elements is rendered as json structure"() {
        given:
        handlers {
            get { ctx ->
                Observable<?> obs = Observable.range(1, 3).map({ cnt -> [id: cnt, value: "some value " + cnt] })
                ChunkedObservableRenderer.doRender(ctx, obs)
            }
        }

        when:
        def result = get()
        then:
        rawResponse().startsWith("""HTTP/1.1 200 OK
transfer-encoding: chunked
content-type: application/json
connection: close
""")
        result.body.text == /[{"id":1,"value":"some value 1"},{"id":2,"value":"some value 2"},{"id":3,"value":"some value 3"}]/
    }

    def "a observable error does not trigger error status"() {
        given:
        handlers {
            get { ctx ->
                Observable<?> obs = Observable
                        .range(1, 3)
                        .delay(10, TimeUnit.MILLISECONDS)
                        .doOnNext({ cnt ->
                    if (cnt == 2) {
                        throw new IllegalStateException()
                    }
                })
                        .map({ cnt -> [id: cnt, value: "some value " + cnt] })
                ChunkedObservableRenderer.doRender(ctx, obs)
            }
        }

        when:
        get()
        then:
        rawResponse().startsWith("""HTTP/1.1 200 OK
transfer-encoding: chunked
content-type: application/json
connection: close
""")
    }
}
