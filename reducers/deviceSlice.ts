import {createAsyncThunk, createSlice} from '@reduxjs/toolkit';
import {apiInstance} from '../app/axiosClient';
import {IDevice} from '../constants/interfaces';

export const createDeviceSlice = createAsyncThunk(
  'device/create',
  async (payload: IDevice) => {
    const {data} = await apiInstance.post('/device', payload);
    return data;
  },
);

export const updateDeviceSlice = createAsyncThunk(
  'device/update',
  async (payload: IDevice) => {
    const {data} = await apiInstance.put('/device', payload);
    return data;
  },
);

export const getDeviceSlice = createAsyncThunk('device/get', async () => {
  const {data} = await apiInstance.get('/device');
  return data;
});

interface IDeviceState {
  loading: 'idle' | 'loading' | 'success' | 'error';
  error?: string;
}

const initialState: IDeviceState = {
  loading: 'idle',
};

export const deviceSlice = createSlice({
  name: 'device',
  initialState,
  reducers: {},
  extraReducers: builder => {
    builder
      .addCase(createDeviceSlice.pending, state => {
        state.loading = 'loading';
      })
      .addCase(createDeviceSlice.fulfilled, state => {
        state.loading = 'success';
      })
      .addCase(createDeviceSlice.rejected, (state, action) => {
        state.loading = 'error';
        state.error = action.error.message;
      });
  },
});

export default deviceSlice.reducer;
