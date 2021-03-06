/*
 * Copyright 2013 the original author or authors.
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

package ratpack.guice.internal;

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.inject.Injector;
import com.google.inject.Provider;
import ratpack.registry.NotInRegistryException;
import ratpack.registry.Registry;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static ratpack.util.ExceptionUtils.uncheck;

public class InjectorBackedRegistry implements Registry {

  final Injector injector;

  public InjectorBackedRegistry(Injector injector) {
    this.injector = injector;
  }

  private final LoadingCache<TypeToken<?>, List<Provider<?>>> cache = CacheBuilder.newBuilder().build(new CacheLoader<TypeToken<?>, List<Provider<?>>>() {
    @Override
    public List<Provider<?>> load(@SuppressWarnings("NullableProblems") TypeToken<?> key) throws Exception {
      @SuppressWarnings({"unchecked", "RedundantCast"})
      List<Provider<?>> providers = (List<Provider<?>>) GuiceUtil.allProvidersOfType(injector, key);
      return providers;
    }
  });

  @Override
  public <O> O get(Class<O> type) throws NotInRegistryException {
    return get(TypeToken.of(type));
  }

  @Override
  public <O> O get(TypeToken<O> type) throws NotInRegistryException {
    O object = maybeGet(type);
    if (object == null) {
      throw new NotInRegistryException(type);
    } else {
      return object;
    }
  }

  public <T> T maybeGet(Class<T> type) {
    return maybeGet(TypeToken.of(type));
  }

  public <T> T maybeGet(TypeToken<T> type) {
    List<Provider<?>> providers = getProviders(type);
    if (providers.isEmpty()) {
      return null;
    } else {
      @SuppressWarnings("unchecked") T cast = (T) providers.get(0).get();
      return cast;
    }
  }

  private <T> List<Provider<?>> getProviders(TypeToken<T> type) {
    try {
      return cache.get(type);
    } catch (ExecutionException e) {
      throw uncheck(e);
    }
  }

  @Override
  public <O> List<O> getAll(Class<O> type) {
    return getAll(TypeToken.of(type));
  }

  @Override
  public <O> List<O> getAll(TypeToken<O> type) {
    List<Provider<?>> providers = getProviders(type);
    if (providers.isEmpty()) {
      return Collections.emptyList();
    } else {
      return Lists.transform(providers, new Function<Provider<?>, O>() {
        @Override
        public O apply(Provider<?> input) {
          @SuppressWarnings("unchecked") O cast = (O) input.get();
          return cast;
        }
      });
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    InjectorBackedRegistry that = (InjectorBackedRegistry) o;

    return injector.equals(that.injector);
  }

  @Override
  public int hashCode() {
    return injector.hashCode();
  }

}
